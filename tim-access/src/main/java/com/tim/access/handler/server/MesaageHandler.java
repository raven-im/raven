package com.tim.access.handler.server;

import com.google.protobuf.MessageLite;
import com.tim.access.config.S2sChannelManager;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.protos.Message.Code;
import com.tim.common.protos.Message.ConverType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
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
public class MesaageHandler extends SimpleChannelInboundHandler<TimMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private S2sChannelManager s2sChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
        TimMessage message) throws Exception {
        if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upDownMessage = message.getUpDownMessage();
            log.info("receive up message:{}",upDownMessage);
            upDownMessage.toBuilder().setId(snowFlake.nextId())
                .getContent().toBuilder().setTime(System.currentTimeMillis());
            if (upDownMessage.getConverType() == ConverType.SINGLE) {
                if (StringUtils.isNotBlank(upDownMessage.getConverId())) {
                    Channel channel = s2sChannelManager
                        .selectSingleChannel(upDownMessage.getConverId());
                    channel.writeAndFlush(message);

                } else if (StringUtils.isNotBlank(upDownMessage.getTargetUid())) {
                    Channel channel = s2sChannelManager
                        .selectSingleChannel(upDownMessage.getTargetUid());
                    channel.writeAndFlush(message);
                } else {
                    sendFail(ctx, upDownMessage);
                }
            } else if (upDownMessage.getConverType() == ConverType.GROUP) {
                if (StringUtils.isNotBlank(upDownMessage.getConverId())) {
                    Channel channel = s2sChannelManager
                        .selectGroupChannel(upDownMessage.getConverId());
                    channel.writeAndFlush(message);
                } else if (StringUtils.isNotBlank(upDownMessage.getTargetUid())) {
                    Channel channel = s2sChannelManager
                        .selectGroupChannel(upDownMessage.getTargetUid());
                    channel.writeAndFlush(message);
                } else {
                    sendFail(ctx, upDownMessage);
                }
            } else {
                sendFail(ctx, upDownMessage);
            }
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
        TimMessage timMessage = TimMessage.newBuilder().setType(Type.MessageAck)
            .setMessageAck(messageAck).build();
        ctx.writeAndFlush(timMessage);
    }
}
