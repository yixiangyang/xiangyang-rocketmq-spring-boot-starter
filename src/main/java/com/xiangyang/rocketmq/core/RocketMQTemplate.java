package com.xiangyang.rocketmq.core;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.xiangyang.rocketmq.config.RocketMQProperties;
import com.xiangyang.rocketmq.constant.RocketMQConstant;
import com.xiangyang.rocketmq.support.LocalMessageStore;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @Author xiangyang
 * @Date 2025/1/16 23:59
 */
@Slf4j
public class RocketMQTemplate {
    private final Producer producer;
    private final LocalMessageStore localMessageStore;
    private final RocketMQProperties properties;

    public RocketMQTemplate(Producer producer, LocalMessageStore localMessageStore,
                            RocketMQProperties properties) {
        this.producer = producer;
        this.localMessageStore = localMessageStore;
        this.properties = properties;
    }

    /**
     * 同步发送消息
     */
    public SendResult syncSend(String topic, String tag, String message) {
        Message msg = createMessage(topic, tag, message);
        try {
            return producer.send(msg, properties.getProducer().getSendMsgTimeout());
        } catch (Exception e) {
            log.error("Send message error: topic={}, tag={}, message={}", topic, tag, message, e);
            throw new RocketMQException("Send message failed", e);
        }
    }

    /**
     * 异步发送消息
     */
    public void asyncSend(String topic, String tag, String message, SendCallback callback) {
        Message msg = createMessage(topic, tag, message);
        try {
            producer.sendAsync(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.debug("Async send message success: topic={}, tag={}, msgId={}",
                            topic, tag, sendResult.getMessageId());
                    callback.onSuccess(sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("Async send message failed: topic={}, tag={}", topic, tag, e);
                    // 发送失败存储到本地消息表
                    localMessageStore.store(new LocalMessage(topic, tag, message));
                    callback.onException(e);
                }
            });
        } catch (Exception e) {
            log.error("Send message error: topic={}, tag={}", topic, tag, e);
            localMessageStore.store(new LocalMessage(topic, tag, message));
            throw new RocketMQException("Send message failed", e);
        }
    }

    private Message createMessage(String topic, String tag, String message) {
        Message msg = new Message();
        msg.setTopic(topic);
        msg.setTag(tag);
        try {
            msg.setBody(message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RocketMQException("Message encoding error", e);
        }
        return msg;
    }
}
