package com.tim.client;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.LoginAck;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConverType;
import com.tim.common.protos.Common.MessageContent;
import com.tim.common.protos.Message.HeartBeat;
import com.tim.common.protos.Message.HeartBeatType;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid = "test1";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendLogin(ctx, uid);
    }

    private void sendLogin(ChannelHandlerContext ctx, String uid) {
        Login login = Login.newBuilder()
            .setUid(uid)
            .setId(888)
            .build();
        ctx.writeAndFlush(login);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg)
        throws Exception {
        if (msg instanceof LoginAck) {
            LoginAck loginAck = (LoginAck) msg;
            log.info("login ack:{}", loginAck.toString());
            log.info("login ack code:{}", loginAck.getCode());
            if (loginAck.getCode() == Code.SUCCESS) {
                int i = 0;
                while (i < 1) {
                    Thread.sleep(1000);
                    MessageContent content = MessageContent.newBuilder().setUid(uid)
                        .setContent("hello world").build();
                    UpDownMessage upDownMessage = UpDownMessage.newBuilder().setCid(11)
                        .setFromUid(uid)
                        .setTargetUid(uid).setConverType(
                            ConverType.SINGLE).setContent(content).build();
                    channelHandlerContext.writeAndFlush(upDownMessage);
                    i++;
                }

            }
        }
//        if (msg instanceof HeartBeat) {
//            HeartBeat heartBeat = (HeartBeat) msg;
//            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
//                HeartBeat heartBeatAck = HeartBeat.newBuilder()
//                    .setId(heartBeat.getId())
//                    .setHeartBeatType(HeartBeatType.PONG)
//                    .build();
//
//                MessageContent content = MessageContent.newBuilder().setUid(uid)
//                    .setContent("hello world").build();
//                UpDownMessage upDownMessage = UpDownMessage.newBuilder().setCid(11)
//                    .setTargetUid(uid).setConverType(
//                        ConverType.SINGLE).setContent(content).build();
//                channelHandlerContext.writeAndFlush(upDownMessage);
//            }
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

}
