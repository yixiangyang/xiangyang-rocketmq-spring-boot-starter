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
@Documented
@Component
public @interface RocketMQMessageListener {
    /**
     * Topic
     */
    String topic();

    /**
     * 消息过滤的 TAG
     */
    String[] tags() default {"*"};

    /**
     * 消费者分组
     */
    String consumerGroup();

    /**
     * 消费模式
     */
    ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;

    /**
     * 最大重试次数
     */
    int maxReconsumeTimes() default -1;

    /**
     * 消费线程数
     */
    int consumeThreadNums() default -1;
}
