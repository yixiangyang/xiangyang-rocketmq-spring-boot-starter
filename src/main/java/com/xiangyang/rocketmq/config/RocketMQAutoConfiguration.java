package com.xiangyang.rocketmq.config;

import com.xiangyang.rocketmq.consumer.RocketMQConsumerContainer;
import com.xiangyang.rocketmq.producer.RocketMQProducerTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
@ConditionalOnProperty(prefix = "rocketmq", value = "enabled", havingValue = "true")
public class RocketMQAutoConfiguration {

    @Bean
    public RocketMQProducerTemplate rocketMQProducerTemplate(RocketMQProperties properties) {
        return new RocketMQProducerTemplate(properties);
    }

    @Bean
    public RocketMQConsumerContainer rocketMQConsumerContainer(RocketMQProperties properties) {
        return new RocketMQConsumerContainer(properties);
    }
}
