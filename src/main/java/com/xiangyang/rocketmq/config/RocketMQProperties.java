package com.xiangyang.rocketmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rocketmq")
@Data
public class RocketMQProperties {
    private String accessKey;
    private String secretKey;
    private String instanceId;
    private String nameServerAddr;

    private Producer producer = new Producer();
//    private Consumer consumer = new Consumer();

    @Data
    public static class Producer {
        private String groupId;
        private Integer sendMsgTimeout = 3000;
        private Integer retryTimesWhenSendFailed = 2;
    }

//    @Data
//    public static class Consumer {
//        private String group;
//        private Integer consumeThreadNums = 20;
//        private Integer maxReconsumeTimes = 16;
//    }
}
