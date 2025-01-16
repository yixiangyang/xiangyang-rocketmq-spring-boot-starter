package com.xiangyang.rocketmq.core;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.xiangyang.rocketmq.config.RocketMQProperties;
import com.xiangyang.rocketmq.constant.RocketMQConstant;
import com.xiangyang.rocketmq.support.LocalMessageStore;

import java.io.UnsupportedEncodingException;

/**
 * @Author xiangyang
 * @Date 2025/1/16 23:59
 */
public class RocketMQTemplate {

    private final Producer producer;
    private final LocalMessageStore localMessageStore;
    private final RocketMQProperties properties;

    public RocketMQTemplate(Producer producer, LocalMessageStore localMessageStore, RocketMQProperties properties) {
        this.producer = producer;
        this.localMessageStore = localMessageStore;
        this.properties = properties;
    }

    public SendResult syncSend(String topic, String tag, String message) {
        return syncSend(topic, tag, message, -1);
    }

    public SendResult syncSend(String topic, String tag, String message, long timeout) {
        Message msg = createMessage(topic, tag, message);
        try {
            if (timeout > 0) {
                return producer.send(msg, timeout);
            }
            return producer.send(msg);
        } catch (Exception e) {
            log.error("Send message error, topic: {}, tag: {}", topic, tag, e);
            throw new RuntimeException("Send message failed", e);
        }
    }

    public void asyncSend(String topic, String tag, String message, SendCallback callback) {
        asyncSend(topic, tag, message, callback, -1);
    }

    public void asyncSend(String topic, String tag, String message, SendCallback callback, long timeout) {
        Message msg = createMessage(topic, tag, message);
        try {
            SendCallback wrappedCallback = new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    if (callback != null) {
                        callback.onSuccess(sendResult);
                    }
                }

                @Override
                public void onException(Throwable e) {
                    log.error("Async send message error, topic: {}, tag: {}", topic, tag, e);
//                    localMessageStore.sentore(topic, tag, message);
                    if (callback != null) {
                        callback.onException(e);
                    }
                }
            };

            if (timeout > 0) {
                producer.sendAsync(msg, wrappedCallback, timeout);
            } else {
                producer.sendAsync(msg, wrappedCallback);
            }
        } catch (Exception e) {
            log.error("Send message error, topic: {}, tag: {}", topic, tag, e);
//            localMessageStore.store(topic, tag, message);
            throw new RuntimeException("Send message failed", e);
        }
    }

    private Message createMessage(String topic, String tag, String message) {
        Message msg = new Message();
        msg.setTopic(topic);
        msg.setTag(tag);
        try {
            msg.setBody(message.getBytes(RocketMQConstant.DEFAULT_CHARSET));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Message encoding error", e);
        }
        return msg;
    }
}
