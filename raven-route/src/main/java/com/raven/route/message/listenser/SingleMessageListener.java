package com.raven.route.message.listenser;

import com.raven.common.kafka.MessageListener;
import com.raven.common.utils.Constants;
import com.raven.route.message.executor.SingleMessageExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SingleMessageListener extends MessageListener<String, String> {

    @Autowired
    private SingleMessageExecutor singleMessageExecutor;

    public SingleMessageListener() {
        this.setTopic(Constants.KAFKA_TOPIC_SINGLE_MSG);
    }

    @Override
    public void receive(String topic, String key, String message) {
        singleMessageExecutor.saveAndSendMsg(message);
    }

}
