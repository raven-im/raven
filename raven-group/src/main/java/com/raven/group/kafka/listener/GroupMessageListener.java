package com.raven.group.kafka.listener;

import com.googlecode.protobuf.format.JsonFormat;
import com.raven.common.kafka.MessageListener;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.Constants;
import com.raven.common.utils.UidUtil;
import com.raven.group.tcp.manager.SenderManager;
import com.raven.storage.conver.ConverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class GroupMessageListener extends MessageListener<String, String> {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private SenderManager senderManager;

    @Autowired
    private ServerChannelManager internalServerChannelManager;

    public GroupMessageListener() {
        this.setTopic(Constants.KAFKA_TOPIC_GROUP_MSG);
    }

    @Override
    public void receive(String topic, String key, String message) {
        log.info("topic=[{}], message=[{}]", topic, message);
        try {
            RavenMessage.Builder builder = RavenMessage.newBuilder();
            JsonFormat.merge(message, builder);
            UpDownMessage upDownMessage = builder.getUpDownMessage();
            cacheAndTransferMsg(upDownMessage);
        } catch (Exception e) {
            log.error("process message error", e);
        }
    }

    private void cacheAndTransferMsg(UpDownMessage upDownMessage) {
        try {
            converManager.cacheMsg2Conver(upDownMessage.getContent(), upDownMessage.getConverId());
            senderManager.sendMessage(upDownMessage);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
