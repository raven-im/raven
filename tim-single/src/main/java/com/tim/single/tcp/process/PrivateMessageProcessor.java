package com.tim.single.tcp.process;

import com.tim.common.enums.AckMessageStatus;
import com.tim.common.protos.Message.MessageAck;
import com.tim.single.tcp.channel.NettyChannelManager;
import com.tim.single.tcp.common.BaseMessageProcessor;
import com.tim.single.tcp.common.OfflineMsgService;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PrivateMessageProcessor implements BaseMessageProcessor {

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
    }



}
