package com.raven.route.message.listenser;

import com.raven.common.kafka.MessageListener;
import com.raven.common.utils.Constants;
import com.raven.route.message.executor.NotificationExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserNotificationListener extends MessageListener<String, String> {

    @Autowired
    private NotificationExecutor executor;

    public UserNotificationListener() {
        this.setTopic(Constants.KAFKA_TOPIC_NOTI_TO_USER);
    }

    @Override
    public void receive(String topic, String key, String message) {
        executor.sendNotification(message);
    }
}
