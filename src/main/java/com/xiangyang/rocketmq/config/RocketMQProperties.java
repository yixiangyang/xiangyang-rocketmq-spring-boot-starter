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
    private String accessKey;
    private String secretKey;
    private String instanceId;
    private String nameServerAddr;

    private Producer producer = new Producer();
    private Consumer consumer = new Consumer();

    @Data
    public static class Producer {
        private String groupId;
        private boolean enabled = true;
        private Map<String, ProducerConfig> configs = new HashMap<>();
    }

    @Data
    public static class Consumer {
        private String groupId;
        private boolean enabled = true;
        private Map<String, ConsumerConfig> configs = new HashMap<>();
    }

    @Data
    public static class ProducerConfig {
        private String topic;
        private String tag;
    }

    @Data
    public static class ConsumerConfig {
        private String topic;
        private Set<String> tags = new HashSet<>();
        private ConsumeMode consumeMode = ConsumeMode.CONCURRENTLY;
    }

    public enum ConsumeMode {
        CONCURRENTLY, ORDERLY
    }
}
