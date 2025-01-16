package com.xiangyang.rocketmq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author xiangyang
 * @Date 2025/1/16 23:59
 */
@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
@ConditionalOnProperty(prefix = "rocketmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RocketMQAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LocalMessageStore localMessageStore() {
        return new LocalMessageStore();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "rocketmq.producer", name = "enabled", havingValue = "true")
    public Producer rocketMQProducer(RocketMQProperties properties) {
        Properties props = new Properties();
        props.setProperty(PropertyKeyConst.AccessKey, properties.getAccessKey());
        props.setProperty(PropertyKeyConst.SecretKey, properties.getSecretKey());
        props.setProperty(PropertyKeyConst.INSTANCE_ID, properties.getInstanceId());
        props.setProperty(PropertyKeyConst.GROUP_ID, properties.getProducer().getGroupId());

        Producer producer = ONSFactory.createProducer(props);
        producer.start();
        return producer;
    }

    @Bean
    @ConditionalOnBean(Producer.class)
    public RocketMQTemplate rocketMQTemplate(Producer producer,
                                             LocalMessageStore localMessageStore,
                                             RocketMQProperties properties) {
        return new RocketMQTemplate(producer, localMessageStore, properties);
    }

    @Bean(name = RocketMQConstant.CONSUMER_CONTAINER_NAME)
    @ConditionalOnProperty(prefix = "rocketmq.consumer", name = "enabled", havingValue = "true")
    public RocketMQConsumerContainer rocketMQConsumerContainer(RocketMQProperties properties) {
        return new RocketMQConsumerContainer(properties);
    }

    @Bean
    @ConditionalOnBean(name = RocketMQConstant.CONSUMER_CONTAINER_NAME)
    public RocketMQListenerAnnotationBeanPostProcessor rocketMQListenerAnnotationProcessor() {
        return new RocketMQListenerAnnotationBeanPostProcessor();
    }
}
