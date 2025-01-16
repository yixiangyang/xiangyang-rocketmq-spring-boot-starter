package com.xiangyang.rocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author xiangyang
 * @Date 2025/1/17 00:00
 */
@ConfigurationProperties(prefix = "rocketmq")
@Data
public class RocketMQProperties {
    /**
     * 是否启用 RocketMQ
     */
    private boolean enabled = true;

    /**
     * AccessKey
     */
    private String accessKey;

    /**
     * SecretKey
     */
    private String secretKey;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 生产者配置
     */
    private Producer producer = new Producer();

    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();

    @Data
    public static class Producer {
        /**
         * 是否启用生产者
         */
        private boolean enabled = true;

        /**
         * 生产者分组
         */
        private String groupId;

        /**
         * 发送超时时间(毫秒)
         */
        private int sendMsgTimeout = 3000;

        /**
         * 重试次数
         */
        private int retryTimes = 3;
    }

    @Data
    public static class Consumer {
        /**
         * 是否启用消费者
         */
        private boolean enabled = true;

        /**
         * 消费者线程数
         */
        private int consumeThreadNums = 20;

        /**
         * 消费失败最大重试次数
         */
        private int maxReconsumeTimes = 16;
    }
}
