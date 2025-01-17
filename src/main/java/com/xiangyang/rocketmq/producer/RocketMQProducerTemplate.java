package com.xiangyang.rocketmq.producer;

import com.aliyun.openservices.ons.api.*;
import com.xiangyang.rocketmq.config.RocketMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Properties;

@Slf4j
public class RocketMQProducerTemplate implements InitializingBean, DisposableBean {

    private final RocketMQProperties properties;
    private Producer producer;

    public RocketMQProducerTemplate(RocketMQProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        Properties props = new Properties();
        props.setProperty(PropertyKeyConst.AccessKey, properties.getAccessKey());
        props.setProperty(PropertyKeyConst.SecretKey, properties.getSecretKey());
        props.setProperty(PropertyKeyConst.INSTANCE_ID, properties.getInstanceId());
        props.setProperty(PropertyKeyConst.GROUP_ID, properties.getProducer().getGroupId());

        producer = ONSFactory.createProducer(props);
        producer.start();
        log.info("RocketMQ producer started");
    }

    public SendResult syncSend(String topic, String tag, String message) {
        Message msg = new Message(topic, tag, message.getBytes());
        return producer.send(msg);
    }

    public void asyncSend(String topic, String tag, String message, SendCallback callback) {
        Message msg = new Message(topic, tag, message.getBytes());
        producer.sendAsync(msg, callback);
    }

    @Override
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
            log.info("RocketMQ producer shutdown");
        }
    }
}
