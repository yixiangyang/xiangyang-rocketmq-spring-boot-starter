package com.xiangyang.rocketmq.consumer;

import com.aliyun.openservices.ons.api.*;
import com.xiangyang.rocketmq.JsonUtils;
import com.xiangyang.rocketmq.config.RocketMQProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RocketMQConsumerContainer implements BeanFactoryAware, ApplicationContextAware,
        SmartInitializingSingleton, DisposableBean {

    private final RocketMQProperties properties;
    private final List<Consumer> consumers = new ArrayList<>();
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private final ExecutorService executorService;

    public RocketMQConsumerContainer(RocketMQProperties properties) {
        this.properties = properties;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("RocketMQConsumerStarter");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RocketMQMessageListener.class);

        beans.forEach((beanName, bean) -> {
            RocketMQMessageListener annotation = bean.getClass().getAnnotation(RocketMQMessageListener.class);
            createConsumer(annotation, bean);
        });

        // 延迟1秒启动消费者
        executorService.execute(() -> {
            try {
                Thread.sleep(1000);
                startConsumers();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Consumer startup interrupted", e);
            } finally {
                executorService.shutdown();
            }
        });
    }

    private void startConsumers() {
        consumers.forEach(consumer -> {
            try {
                consumer.start();
                log.info("RocketMQ consumer started: {}", consumer);
            } catch (Exception e) {
                log.error("Failed to start consumer: {}", consumer, e);
            }
        });
        log.info("All RocketMQ consumers started, total count: {}", consumers.size());
    }

    private void createConsumer(RocketMQMessageListener annotation, Object bean) {
        try {
            validateAnnotation(annotation);

            Properties props = buildConsumerProperties(annotation);
            Consumer consumer = ONSFactory.createConsumer(props);

            String subscription = buildSubscription(annotation.tags());
            consumer.subscribe(annotation.topic(), subscription, (message, context) -> {
                try {
                    return processMessage(message, bean);
                } catch (Exception e) {
                    log.error("Process message error, topic: {}, msgId: {}",
                            message.getTopic(), message.getMsgID(), e);
                    return Action.ReconsumeLater;
                }
            });

            consumers.add(consumer);
            log.info("Created consumer for topic: {}, tags: {}, group: {}",
                    annotation.topic(), annotation.tags(), annotation.consumerGroup());

        } catch (Exception e) {
            log.error("Failed to create consumer for bean: {}", bean.getClass().getName(), e);
            throw new RuntimeException("Failed to create consumer", e);
        }
    }

    private void validateAnnotation(RocketMQMessageListener annotation) {
        if (StringUtils.isEmpty(annotation.topic())) {
            throw new IllegalArgumentException("Topic cannot be empty");
        }
        if (StringUtils.isEmpty(annotation.consumerGroup())) {
            throw new IllegalArgumentException("ConsumerGroup cannot be empty");
        }
    }

    private Properties buildConsumerProperties(RocketMQMessageListener annotation) {
        Properties props = new Properties();
        props.setProperty(PropertyKeyConst.AccessKey, properties.getAccessKey());
        props.setProperty(PropertyKeyConst.SecretKey, properties.getSecretKey());
        props.setProperty(PropertyKeyConst.INSTANCE_ID, properties.getInstanceId());
        props.setProperty(PropertyKeyConst.GROUP_ID, annotation.consumerGroup());

//        // 设置消费线程数
//        if (properties.getConsumer().getConsumeThreadNums() != null) {
//            props.setProperty(PropertyKeyConst.ConsumeThreadNums,
//                    String.valueOf(properties.getConsumer().getConsumeThreadNums()));
//        }
//
//        // 设置最大重试次数
//        if (properties.getConsumer().getMaxReconsumeTimes() != null) {
//            props.setProperty(PropertyKeyConst.MaxReconsumeTimes,
//                    String.valueOf(properties.getConsumer().getMaxReconsumeTimes()));
//        }

        return props;
    }

    private String buildSubscription(String[] tags) {
        if (tags == null || tags.length == 0 || (tags.length == 1 && "*".equals(tags[0]))) {
            return "*";
        }
        return String.join("||", tags);
    }

    private Action processMessage(Message message, Object bean) throws Exception {
        Method consumeMethod = findConsumeMethod(bean);
        if (consumeMethod == null) {
            throw new IllegalStateException("No valid consume method found in consumer: "
                    + bean.getClass().getName());
        }

        // 设置方法可访问
        consumeMethod.setAccessible(true);

        // 根据方法参数类型进行不同的处理
        Class<?> parameterType = consumeMethod.getParameterTypes()[0];
        Object result;

        if (parameterType == Message.class) {
            result = consumeMethod.invoke(bean, message);
        } else if (parameterType == String.class) {
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            result = consumeMethod.invoke(bean, messageBody);
        } else {
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            Object param = JsonUtils.parseObject(messageBody, parameterType);
            result = consumeMethod.invoke(bean, param);
        }

        // 处理返回值
        if (result instanceof Action) {
            return (Action) result;
        }
        return Action.CommitMessage;
    }

    private Method findConsumeMethod(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (isValidConsumeMethod(method)) {
                return method;
            }
        }
        return null;
    }

    private boolean isValidConsumeMethod(Method method) {
        // 方法必须有且仅有一个参数
        if (method.getParameterCount() != 1) {
            return false;
        }

        // 检查参数类型（支持 Message、String 或自定义对象）
        Class<?> parameterType = method.getParameterTypes()[0];
        return Message.class.isAssignableFrom(parameterType)
                || String.class.isAssignableFrom(parameterType)
                || parameterType.isAnnotationPresent(Data.class);
    }

    @Override
    public void destroy() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        consumers.forEach(consumer -> {
            try {
                consumer.shutdown();
                log.info("Shutdown consumer: {}", consumer);
            } catch (Exception e) {
                log.error("Error occurred when shutting down consumer: {}", consumer, e);
            }
        });
        consumers.clear();
        log.info("All RocketMQ consumers have been shutdown");
    }
}