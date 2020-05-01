package com.raven.storeredis.message.listenser;

import com.raven.common.kafka.MessageListener;
import com.raven.common.utils.Constants;
import com.raven.storeredis.config.CustomConfig;
import com.raven.storeredis.message.processor.NotifyProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener extends MessageListener<String, String> {

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redis;

    public NotificationListener() {
        this.setTopic(Constants.KAFKA_TOPIC_NOTI_TO_CONV);
    }

    @Override
    public void receive(String topic, String key, String message) {
        NotifyProcessor processor = new NotifyProcessor(message, redis);
        CustomConfig.threadPool.submit(processor);
    }
}
