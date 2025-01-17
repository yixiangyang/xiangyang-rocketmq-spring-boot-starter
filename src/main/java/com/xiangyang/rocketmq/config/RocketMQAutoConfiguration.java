package com.xiangyang.rocketmq.config;

import com.xiangyang.rocketmq.consumer.RocketMQConsumerContainer;
import com.xiangyang.rocketmq.producer.RocketMQProducerTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;


@AutoConfiguration
@EnableConfigurationProperties(RocketMQProperties.class)
@ConditionalOnProperty(prefix = "rocketmq", value = "enabled", havingValue = "true", matchIfMissing = true)
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@ComponentScan(basePackages = "com.xiangyang.rocketmq")
public class RocketMQAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rocketmq.producer", value = "enabled", havingValue = "true")
    public RocketMQProducerTemplate rocketMQProducerTemplate(RocketMQProperties properties) {
        return new RocketMQProducerTemplate(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RocketMQConsumerContainer rocketMQConsumerContainer(RocketMQProperties properties) {
        return new RocketMQConsumerContainer(properties);
    }
}
