package com.tim.access.handler;

import com.google.protobuf.MessageLite;
import com.tim.access.channel.NettyChannelManager;
import com.tim.common.protos.Message.MessageAck;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MesaageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof MessageAck) {

        }
        channelHandlerContext.fireChannelRead(messageLite);
    }

}
