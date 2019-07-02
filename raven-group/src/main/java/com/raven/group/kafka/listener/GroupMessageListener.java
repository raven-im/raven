package com.raven.group.kafka.listener;

import com.googlecode.protobuf.format.JsonFormat;
import com.raven.common.kafka.MessageListener;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.Constants;
import com.raven.group.tcp.manager.GroupMessageExecutor;
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
        try {
            RavenMessage.Builder builder = RavenMessage.newBuilder();
            JsonFormat.merge(message, builder);
            UpDownMessage upDownMessage = builder.getUpDownMessage();
            saveAndSendMsg(upDownMessage);
        } catch (Exception e) {
            log.error("process message error", e);
        }
    }

    private void saveAndSendMsg(UpDownMessage upDownMessage) {
        try {
            groupMessageExecutor.sendMessage(upDownMessage);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
