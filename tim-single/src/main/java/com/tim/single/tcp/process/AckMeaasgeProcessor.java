package com.tim.single.tcp.process;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Message.MessageAck;
import com.tim.single.tcp.channel.NettyChannelManager;
import com.tim.single.tcp.common.BaseMessageProcessor;
import com.tim.single.tcp.common.OfflineMsgService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
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
    }


}
