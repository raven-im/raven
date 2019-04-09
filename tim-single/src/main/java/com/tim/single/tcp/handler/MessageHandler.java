package com.tim.single.tcp.handler;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.single.tcp.manager.ConversationManager;
import com.tim.single.tcp.manager.SenderManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;

@Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    @Lazy
    private ConversationManager conversationManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite) throws Exception {
        if (messageLite instanceof UpDownMessage) {
            UpDownMessage message = (UpDownMessage) messageLite;
            if (message.getConversationType() == ConversationType.SINGLE &&
                StringUtils.isEmpty(message.getConversasionId())) {
                log.info("received msg id:{}", message.getId());
                // access server ACK.
                String convId = conversationManager.cacheConversation(message);
                sendACK(ctx, message.getId(), message.getFromId(), Code.SUCCESS, convId);

                //TODO message direction  SS  -> SC
                SenderManager.addMessage(message);
            } else {
                log.error("illegal Message.");
                sendACK(ctx, message.getId(), message.getFromId(), Code.FAIL, "");
            }
        }
    }

    private void sendACK(ChannelHandlerContext ctx, long id, String targetId, Code code, String convId) {
        MessageAck messageAck = MessageAck.newBuilder()
            .setId(id)
            .setTargetId(targetId)
            .setCode(code)
            .setTime(System.currentTimeMillis())
            .setConversasionId(convId)
            .build();
        ctx.writeAndFlush(messageAck);
    }

}
