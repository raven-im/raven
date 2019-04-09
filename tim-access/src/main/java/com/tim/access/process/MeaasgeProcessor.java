package com.tim.access.process;

import com.google.protobuf.MessageLite;
import com.tim.access.offline.OfflineMsgService;
import com.tim.common.netty.BaseMessageProcessor;
import com.tim.common.netty.ServerChannelManager;
import com.tim.common.protos.Message.MessageAck;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MeaasgeProcessor implements BaseMessageProcessor {

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private ServerChannelManager uidChannelManager;

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        MessageAck ackMessage = (MessageAck) messageLite;
        String fromUid = uidChannelManager.getIdByChannel(context.channel());
        log.info("fromUid:{}", fromUid);
    }


}
