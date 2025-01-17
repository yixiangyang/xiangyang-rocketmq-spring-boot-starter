# xiangyang-rocketmq-spring-boot-starter

```java
# 代码使用
package com.example.demo;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.Message;
import com.xiangyang.rocketmq.consumer.RocketMQMessageListener;
import org.springframework.stereotype.Component;

@RocketMQMessageListener(
        topic = "DEFAULT",
        tags = {"USER_ACTION_MT"},
        consumerGroup = "GID_USER_ACTION_MT"
)
@Component
public class MessageConsumer {

    public Action consume(Message message) {
        String body = new String(message.getBody());
        System.out.println("这个是收到的消息 "+body);
        // 处理消息
        return Action.CommitMessage;
    }
}

```

```yaml
# 配置
rocketmq:
  enabled: true
  access-key: aaa
  secret-key: aaa
  #  instance-id: rmq-cn-v
  name-server-addr: localhost:8080
  #  producer:
  #    group-id: GID_PRODUCER
  consumer:
    enabled: true
server:
  port: 7878
```