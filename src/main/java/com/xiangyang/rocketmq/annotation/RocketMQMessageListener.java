package com.xiangyang.rocketmq.annotation;

import com.xiangyang.rocketmq.config.RocketMQProperties.ConsumeMode;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author xiangyang
 * @Date 2025/1/17 00:00
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RocketMQMessageListener {
    String topic();
    String[] tags() default {"*"};
    String consumerGroup();
    ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;
}
