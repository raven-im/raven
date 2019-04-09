package com.tim.single.tcp.handler;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.single.tcp.SenderManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite) throws Exception {
        if (messageLite instanceof UpDownMessage) {
            UpDownMessage message = (UpDownMessage) messageLite;
            if (message.getConversationType() == ConversationType.SINGLE) {
                log.info("received msg id:{}", message.getId());
                // access server ACK.
                MessageAck messageAck = MessageAck.newBuilder()
                    .setId(message.getId())
                    .setTargetId(message.getFromId())
                    .setCode(Code.SUCCESS)
                    .setTime(System.currentTimeMillis())
                    .setConversasionId(message.getConversasionId())
                    .build();
                ctx.writeAndFlush(messageAck);

                //TODO  redis save.

                //TODO route the message to target ACCESS.
                SenderManager.addMessage(message);
            }
        } else {
            ctx.fireChannelRead(messageLite);
        }
    }

}
