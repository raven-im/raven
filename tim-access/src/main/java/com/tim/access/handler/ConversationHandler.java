package com.tim.access.handler;

import com.google.protobuf.MessageLite;
import com.tim.common.netty.ChannelManager;
import com.tim.common.protos.Conversation;
import com.tim.common.protos.Conversation.ConversationReq;
import com.tim.common.protos.Conversation.OperationType;
import com.tim.common.protos.Message.MessageAck;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Sharable
@Slf4j
public class ConversationHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private ChannelManager uidChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof ConversationReq) {
            ConversationReq conversationReq = (ConversationReq) messageLite;
            if (conversationReq.getType() == OperationType.DETAIL) {

            } else if (conversationReq.getType() == OperationType.ALL) {

            }

        } else {
            channelHandlerContext.fireChannelRead(messageLite);
        }
    }

}