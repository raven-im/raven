package com.raven.storeredis.message.listenser;

import com.raven.common.kafka.MessageListener;
import com.raven.common.utils.Constants;
import com.raven.storeredis.config.CustomConfig;
import com.raven.storeredis.message.processor.MessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SingleMessageListener extends MessageListener<String, String> {

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate redis;

    public SingleMessageListener() {
        this.setTopic(Constants.KAFKA_TOPIC_SINGLE_MSG);
    }

    @Override
    public void receive(String topic, String key, String message) {
        MessageProcessor processor = new MessageProcessor(message, redis);
        CustomConfig.threadPool.submit(processor);
    }
}
