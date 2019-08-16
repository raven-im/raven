package com.raven.route.message.listenser;

import com.raven.common.kafka.MessageListener;
import com.raven.common.utils.Constants;
import com.raven.route.message.executor.NotifyConvExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConvNotificationListener extends MessageListener<String, String> {

    @Autowired
    private NotifyConvExecutor executor;

    public ConvNotificationListener() {
        this.setTopic(Constants.KAFKA_TOPIC_NOTI_TO_CONV);
    }

    @Override
    public void receive(String topic, String key, String message) {
        executor.sendNotification(message);
    }
}
