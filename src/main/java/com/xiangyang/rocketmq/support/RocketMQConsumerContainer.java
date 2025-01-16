package com.xiangyang.rocketmq.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

/**
 * @Author xiangyang
 * @Date 2025/1/16 23:58
 */
@Slf4j
public class RocketMQConsumerContainer implements SmartLifecycle {
    private final List<Consumer> consumers = new CopyOnWriteArrayList<>();
    private final RocketMQProperties properties;
    private volatile boolean running = false;

    public RocketMQConsumerContainer(RocketMQProperties properties) {
        this.properties = properties;
    }

    public void registerConsumer(RocketMQMessageListener annotation, MessageListener listener) {
        Properties props = new Properties();
        props.setProperty(PropertyKeyConst.AccessKey, properties.getAccessKey());
        props.setProperty(PropertyKeyConst.SecretKey, properties.getSecretKey());
        props.setProperty(PropertyKeyConst.INSTANCE_ID, properties.getInstanceId());
        props.setProperty(PropertyKeyConst.GROUP_ID, annotation.consumerGroup());

        // 设置消费线程数
        int consumeThreadNums = annotation.consumeThreadNums() > 0 ?
                annotation.consumeThreadNums() :
                properties.getConsumer().getConsumeThreadNums();
        props.setProperty(PropertyKeyConst.ConsumeThreadNums,
                String.valueOf(consumeThreadNums));

        // 设置最大重试次数
        int maxReconsumeTimes = annotation.maxReconsumeTimes() > 0 ?
                annotation.maxReconsumeTimes() :
                properties.getConsumer().getMaxReconsumeTimes();
        props.setProperty(PropertyKeyConst.MaxReconsumeTimes,
                String.valueOf(maxReconsumeTimes));

        Consumer consumer = ONSFactory.createConsumer(props);
        String topic = annotation.topic();
        String[] tags = annotation.tags();

        for (String tag : tags) {
            consumer.subscribe(topic, tag, listener);
            log.info("Register consumer: topic={}, tag={}, group={}",
                    topic, tag, annotation.consumerGroup());
        }

        consumers.add(consumer);
    }

    @Override
    public void start() {
        if (!isRunning()) {
            try {
                // 延迟1秒启动消费者
                Thread.sleep(1000);
                for (Consumer consumer : consumers) {
                    consumer.start();
                }
                running = true;
                log.info("RocketMQ consumer container started, consumer size: {}",
                        consumers.size());
            } catch (Exception e) {
                log.error("Start RocketMQ consumer container error", e);
                throw new RocketMQException("Start consumer container failed", e);
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            for (Consumer consumer : consumers) {
                consumer.shutdown();
            }
            consumers.clear();
            running = false;
            log.info("RocketMQ consumer container stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 确保消费者在其他组件之后启动，之前停止
        return Integer.MAX_VALUE;
    }
}
