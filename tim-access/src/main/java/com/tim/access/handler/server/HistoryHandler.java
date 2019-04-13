package com.tim.access.handler.server;

import com.google.protobuf.MessageLite;
import com.tim.common.netty.IdChannelManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class HistoryHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        channelHandlerContext.fireChannelRead(messageLite);
    }

}
