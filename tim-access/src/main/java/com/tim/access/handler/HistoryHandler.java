package com.tim.access.handler;

import com.google.protobuf.MessageLite;
import com.tim.common.netty.ServerChannelManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class HistoryHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private ServerChannelManager uidChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        channelHandlerContext.fireChannelRead(messageLite);
    }

}
