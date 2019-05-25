package com.raven.client.group;

import com.raven.client.common.Utils;
import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.HeartBeat;
import com.raven.common.protos.Message.HeartBeatType;
import com.raven.common.protos.Message.Login;
import com.raven.common.protos.Message.LoginAck;
import com.raven.common.protos.Message.MessageAck;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.MessageType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientInvitee1Handler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid = "invitee1";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendLogin(ctx, uid);
    }

    private void sendLogin(ChannelHandlerContext ctx, String uid) {
        String token = Utils.getToken(uid);
        Login login = Login.newBuilder()
            .setUid(uid)
            .setId(ClientOwner.snowFlake.nextId())
            .setToken(token)
            .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.Login).setLogin(login)
            .build();
        ctx.writeAndFlush(ravenMessage);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message)
        throws Exception {
        if (message.getType() == Type.LoginAck) {
            LoginAck loginAck = message.getLoginAck();
            log.info("login ack:{}", loginAck.toString());
            if (loginAck.getCode() == Code.SUCCESS) {
                log.info("login success. {} waiting for messages", uid);
            }
        } else if (message.getType() == Type.MessageAck) {
            MessageAck messageAck = message.getMessageAck();
            log.info("receive message ack:{}", messageAck);
        } else if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upDownMessage = message.getUpDownMessage();
            log.info("receive down message:{}", upDownMessage);
            MessageContent content = MessageContent.newBuilder()
                .setId(ClientOwner.snowFlake.nextId())
                .setUid(uid)
                .setTime(System.currentTimeMillis())
                .setType(MessageType.TEXT)
                .setContent("hello world.")
                .build();
            UpDownMessage msg = UpDownMessage.newBuilder()
                .setCid(ClientOwner.snowFlake.nextId())
                .setFromUid(uid)
                .setConverId(upDownMessage.getConverId())
                .setConverType(ConverType.GROUP)
                .setContent(content)
                .build();
            RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
                .setUpDownMessage(msg).build();
            ctx.writeAndFlush(ravenMessage);
        } else if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
            log.info("receive hearbeat :{}", heartBeat);
            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
                HeartBeat heartBeatAck = HeartBeat.newBuilder()
                    .setId(heartBeat.getId())
                    .setHeartBeatType(HeartBeatType.PONG)
                    .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.HeartBeat)
                    .setHeartBeat(heartBeatAck).build();
                ctx.writeAndFlush(ravenMessage);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

}
