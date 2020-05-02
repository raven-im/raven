package com.raven.client.group;

import com.raven.common.protos.Message.*;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.SnowFlake;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ClientInviteeHandler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid;
    private String token;
    private SnowFlake snowFlake;

    public ClientInviteeHandler(String uid, String token, SnowFlake snowFlake) {
        super(true);
        this.uid = uid;
        this.token = token;
        this.snowFlake = snowFlake;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendLogin(ctx, uid);
    }

    private void sendLogin(ChannelHandlerContext ctx, String uid) {
        Login login = Login.newBuilder()
                .setUid(uid)
                .setId(snowFlake.nextId())
                .setToken(token)
                .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder()
                .setType(Type.Login)
                .setLogin(login)
                .build();
        ctx.writeAndFlush(ravenMessage);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {
        if (message.getType() == Type.LoginAck) {
            LoginAck loginAck = message.getLoginAck();
            log.info("login ack:{}", loginAck.toString());
            if (loginAck.getCode() == Code.SUCCESS) {
                log.info("login success. {} waiting for messages", uid);
            }
        } else if (message.getType() == Type.MessageAck) {
            MessageAck messageAck = message.getMessageAck();
            log.info("receive message ack:{}", JsonHelper.toJsonString(messageAck));
        } else if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upDownMessage = message.getUpDownMessage();
            log.info("receive down message:{}", JsonHelper.toJsonString(upDownMessage));
//            MessageContent content = MessageContent.newBuilder()
//                .setId(snowFlake.nextId())
//                .setUid(uid)
//                .setTime(System.currentTimeMillis())
//                .setType(MessageType.TEXT)
//                .setContent("hello world.")
//                .build();
//            UpDownMessage msg = UpDownMessage.newBuilder()
//                .setCid(snowFlake.nextId())
//                .setFromUid(uid)
//                .setConverId(upDownMessage.getConverId())
//                .setConverType(ConverType.GROUP)
//                .setContent(content)
//                .build();
//            RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
//                .setUpDownMessage(msg).build();
//            ctx.writeAndFlush(ravenMessage);
        } else if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
//            log.info("receive heartbeat :{}", JsonHelper.toJsonString(heartBeat));
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