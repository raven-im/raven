package com.tim.access.handler.server;

import com.google.protobuf.MessageLite;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.protos.Message.TimMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class HistoryHandler extends SimpleChannelInboundHandler<TimMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        TimMessage message) throws Exception {
        channelHandlerContext.fireChannelRead(message);
    }

}
