package com.tim.server.process;

import com.tim.common.protos.Message.MessageAck;
import com.tim.server.channel.NettyChannelManager;
import com.tim.server.common.BaseMessageProcessor;
import com.tim.server.common.OfflineMsgService;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class AckMeaasgeProcessor implements BaseMessageProcessor {

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        MessageAck ackMessage = (MessageAck) messageLite;
        String fromUid = nettyChannelManager.getUidByChannel(context.channel());
        log.info("fromUid:{}", fromUid);
        offLineMsgService.deleteAckMessage(fromUid, ackMessage.getId(),
            ackMessage.getTimestamp());
    }


}
