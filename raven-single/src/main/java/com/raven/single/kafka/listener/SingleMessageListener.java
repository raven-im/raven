package com.raven.single.kafka.listener;

import com.googlecode.protobuf.format.JsonFormat;
import com.raven.common.kafka.MessageListener;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.Constants;
import com.raven.single.tcp.manager.SenderManager;
import com.raven.storage.conver.ConverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SingleMessageListener extends MessageListener<String, String> {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private SenderManager senderManager;

    public SingleMessageListener() {
        this.setTopic(Constants.KAFKA_TOPIC_SINGLE_MSG);
    }

    @Override
    public void receive(String topic, String key, String message) {
        log.info("topic=[{}], message=[{}]", topic, message);
        try {
            RavenMessage.Builder builder = RavenMessage.newBuilder();
            JsonFormat.merge(message, builder);
            UpDownMessage upDownMessage = builder.getUpDownMessage();
            String convId = upDownMessage.getConverId();
            converManager.saveMsg2Conver(upDownMessage.getContent(), convId);
            UpDownMessage downMessage = UpDownMessage.newBuilder()
                .setId(upDownMessage.getId())
                .setFromUid(upDownMessage.getFromUid())
                .setTargetUid(upDownMessage.getTargetUid())
                .setConverType(upDownMessage.getConverType())
                .setContent(upDownMessage.getContent())
                .setConverId(convId)
                .build();
            senderManager.sendMessage(downMessage);
        } catch (Exception e) {
            log.error("process message error", e);
        }

    }
}
