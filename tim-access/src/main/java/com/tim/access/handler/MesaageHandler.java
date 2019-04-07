package com.tim.access.handler;

import com.google.protobuf.MessageLite;
import com.tim.access.server.AccessTcpServer;
import com.tim.common.netty.ServerChannelManager;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.common.utils.SnowFlake;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Sharable
@Slf4j
public class MesaageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private ServerChannelManager uidChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof UpDownMessage) {
            UpDownMessage message = (UpDownMessage) messageLite;
            message.toBuilder().setId(snowFlake.nextId())
                .getContent().toBuilder().setTime(System.currentTimeMillis());
                // TODO 转发单聊消息
            if (message.getConversationType() == ConversationType.SINGLE) {
                // TODO 转发群聊消息
            } else if (message.getConversationType() == ConversationType.GROUP) {

            } else {
                MessageAck messageAck = MessageAck.newBuilder()
                    .setClientId(message.getClientId())
                    .setTargetId(uidChannelManager.getIdByChannel(ctx.channel()))
                    .setCode(Code.FAIL)
                    .setTime(System.currentTimeMillis())
                    .setConversasionId(message.getConversasionId())
                    .build();
                ctx.writeAndFlush(messageAck);
            }
        } else {
            ctx.fireChannelRead(messageLite);
        }
    }

}
