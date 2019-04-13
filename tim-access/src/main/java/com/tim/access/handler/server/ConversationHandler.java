package com.tim.access.handler.server;

import com.tim.common.netty.IdChannelManager;
import com.tim.common.protos.Message.ConverReq;
import com.tim.common.protos.Message.OperationType;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class ConversationHandler extends SimpleChannelInboundHandler<TimMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        TimMessage message) throws Exception {
        if (message.getType() == Type.ConverReq) {
            ConverReq conversationReq = message.getConverReq();
            if (conversationReq.getType() == OperationType.DETAIL) {

            } else if (conversationReq.getType() == OperationType.ALL) {

            }
        }
    }

}