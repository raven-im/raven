package com.tim.access.process;

import com.google.protobuf.MessageLite;
import com.tim.access.channel.NettyChannelManager;
import com.tim.access.offline.OfflineMsgService;
import com.tim.common.netty.BaseMessageProcessor;
import com.tim.common.protos.Message.MessageAck;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MeaasgeProcessor implements BaseMessageProcessor {

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        MessageAck ackMessage = (MessageAck) messageLite;
        String fromUid = nettyChannelManager.getUidByChannel(context.channel());
        log.info("fromUid:{}", fromUid);
    }


}
