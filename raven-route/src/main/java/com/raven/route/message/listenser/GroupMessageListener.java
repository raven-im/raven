package com.raven.route.message.listenser;

import com.raven.common.kafka.MessageListener;
import com.raven.common.utils.Constants;
import com.raven.route.message.executor.GroupMessageExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupMessageListener extends MessageListener<String, String> {

    @Autowired
    private GroupMessageExecutor groupMessageExecutor;

    public GroupMessageListener() {
        this.setTopic(Constants.KAFKA_TOPIC_GROUP_MSG);
    }

    @Override
    public void receive(String topic, String key, String message) {
        groupMessageExecutor.saveAndSendMsg(message);
    }
}
