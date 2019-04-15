package com.tim.access.handler.server;

import com.google.protobuf.MessageLite;
import com.tim.access.config.S2sChannelManager;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.protos.Message.Code;
import com.tim.common.protos.Message.ConverType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.MessageContent;
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
            UpDownMessage upMessage = message.getUpDownMessage();
            log.info("receive up message:{}", upMessage);
            if (upMessage.getConverType() == ConverType.SINGLE) {
                if (StringUtils.isNotBlank(upMessage.getConverId())) {
                    Channel channel = s2sChannelManager
                        .selectSingleChannel(upMessage.getConverId());
                    channel.writeAndFlush(buildTimMessage(ctx, upMessage));
                } else if (StringUtils.isNotBlank(upMessage.getTargetUid())) {
                    Channel channel = s2sChannelManager
                        .selectSingleChannel(upMessage.getTargetUid());
                    channel.writeAndFlush(buildTimMessage(ctx, upMessage));
                } else {
                    sendFailAck(ctx, upMessage);
                }
            } else if (upMessage.getConverType() == ConverType.GROUP) {
                if (StringUtils.isNotBlank(upMessage.getConverId())) {
                    Channel channel = s2sChannelManager
                        .selectGroupChannel(upMessage.getConverId());
                    channel.writeAndFlush(buildTimMessage(ctx, upMessage));
                } else if (StringUtils.isNotBlank(upMessage.getTargetUid())) {
                    Channel channel = s2sChannelManager
                        .selectGroupChannel(upMessage.getTargetUid());
                    channel.writeAndFlush(buildTimMessage(ctx, upMessage));
                } else {
                    sendFailAck(ctx, upMessage);
                }
            } else {
                sendFailAck(ctx, upMessage);
            }
        }
    }

    private void sendFailAck(ChannelHandlerContext ctx, UpDownMessage message) {
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

    private TimMessage buildTimMessage(ChannelHandlerContext ctx, UpDownMessage upDownMessage) {
        Long id = snowFlake.nextId();
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        MessageContent content = MessageContent.newBuilder().setId(id)
            .setType(upDownMessage.getContent().getType()).setUid(uid)
            .setContent(upDownMessage.getContent().getContent())
            .setTime(System.currentTimeMillis()).build();
        UpDownMessage upMesaage = UpDownMessage.newBuilder().setId(id)
            .setCid(upDownMessage.getCid()).setFromUid(uid)
            .setTargetUid(upDownMessage.getTargetUid()).setContent(content)
            .setConverId(upDownMessage.getConverId()).setConverType(upDownMessage.getConverType())
            .setGroupId(upDownMessage.getGroupId())
            .build();
        TimMessage timMessage = TimMessage.newBuilder().setType(Type.UpDownMessage)
            .setUpDownMessage(upMesaage).build();
        return timMessage;
    }
}
