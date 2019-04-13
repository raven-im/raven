package com.tim.access.handler.server;

import com.google.protobuf.MessageLite;
import com.tim.access.config.S2sChannelManager;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConverType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.common.utils.SnowFlake;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class MesaageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private S2sChannelManager s2sChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof UpDownMessage) {
            UpDownMessage message = (UpDownMessage) messageLite;
            message.toBuilder().setId(snowFlake.nextId())
                .getContent().toBuilder().setTime(System.currentTimeMillis());
            if (message.getConverType() == ConverType.SINGLE) {
                if (StringUtils.isNotBlank(message.getConverId())) {
                    Channel channel = s2sChannelManager
                        .selectSingleChannel(message.getConverId());
                    channel.writeAndFlush(message);
                } else if (StringUtils.isNotBlank(message.getTargetUid())) {
                    Channel channel = s2sChannelManager.selectSingleChannel(message.getTargetUid());
                    channel.writeAndFlush(message);
                } else {
                    sendFail(ctx, message);
                }
            } else if (message.getConverType() == ConverType.GROUP) {
                if (StringUtils.isNotBlank(message.getConverId())) {
                    Channel channel = s2sChannelManager
                        .selectGroupChannel(message.getConverId());
                    channel.writeAndFlush(message);
                } else if (StringUtils.isNotBlank(message.getTargetUid())) {
                    Channel channel = s2sChannelManager.selectGroupChannel(message.getTargetUid());
                    channel.writeAndFlush(message);
                } else {
                    sendFail(ctx, message);
                }
            } else {
                sendFail(ctx, message);
            }
        } else {
            ctx.fireChannelRead(messageLite);
        }
    }

    private void sendFail(ChannelHandlerContext ctx, UpDownMessage message) {
        MessageAck messageAck = MessageAck.newBuilder()
            .setCid(message.getCid())
            .setTargetUid(uidChannelManager.getIdByChannel(ctx.channel()))
            .setCode(Code.FAIL)
            .setTime(System.currentTimeMillis())
            .setConverId(message.getConverId())
            .build();
        ctx.writeAndFlush(messageAck);
    }
}
