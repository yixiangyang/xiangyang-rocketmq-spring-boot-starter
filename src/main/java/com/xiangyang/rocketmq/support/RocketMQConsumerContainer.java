package com.xiangyang.rocketmq.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

/**
 * @Author xiangyang
 * @Date 2025/1/16 23:58
 */
@Slf4j
public class RocketMQConsumerContainer implements SmartLifecycle {
    private final List<Consumer> consumers = new ArrayList<>();
    private final RocketMQProperties properties;
    private volatile boolean running = false;

    public RocketMQConsumerContainer(RocketMQProperties properties) {
        this.properties = properties;
    }

    public void registerConsumer(String topic, String[] tags, String consumerGroup,
                                 MessageListener messageListener) {
        Properties props = new Properties();
        props.setProperty(PropertyKeyConst.AccessKey, properties.getAccessKey());
        props.setProperty(PropertyKeyConst.SecretKey, properties.getSecretKey());
        props.setProperty(PropertyKeyConst.INSTANCE_ID, properties.getInstanceId());
        props.setProperty(PropertyKeyConst.GROUP_ID, consumerGroup);
        props.setProperty(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);

        Consumer consumer = ONSFactory.createConsumer(props);
        for (String tag : tags) {
            consumer.subscribe(topic, tag, messageListener);
        }
        consumers.add(consumer);
    }

    @Override
    public void start() {
        if (!running) {
            try {
                Thread.sleep(RocketMQConstant.CONSUMER_START_DELAY_TIME);
                for (Consumer consumer : consumers) {
                    consumer.start();
                }
                running = true;
                log.info("RocketMQ consumers started");
            } catch (Exception e) {
                log.error("Start RocketMQ consumers error", e);
                throw new RuntimeException("Start RocketMQ consumers failed", e);
            }
        }
    }

    @Override
    public void stop() {
        if (running) {
            for (Consumer consumer : consumers) {
                consumer.shutdown();
            }
            running = false;
            log.info("RocketMQ consumers stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
