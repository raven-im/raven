package com.tim.client.single;

import com.tim.common.protos.Message.Code;
import com.tim.common.protos.Message.ConverAck;
import com.tim.common.protos.Message.ConverReq;
import com.tim.common.protos.Message.ConverType;
import com.tim.common.protos.Message.HeartBeat;
import com.tim.common.protos.Message.Login;
import com.tim.common.protos.Message.LoginAck;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.MessageContent;
import com.tim.common.protos.Message.MessageType;
import com.tim.common.protos.Message.OperationType;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientFromHandler extends SimpleChannelInboundHandler<TimMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid = "test2";

    private String[] toUidList = {"test3", "test1", "test4", "test5", "test6"};


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
        TimMessage timMessage = TimMessage.newBuilder().setType(Type.Login).setLogin(login).build();
        ctx.writeAndFlush(timMessage);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TimMessage message)
        throws Exception {
        if (message.getType() == Type.LoginAck) {
            LoginAck loginAck = message.getLoginAck();
            log.info("login ack:{}", loginAck.toString());
            if (loginAck.getCode() == Code.SUCCESS) {
                for (String toUid : toUidList) {
                    Thread.sleep(1000);
                    MessageContent content = MessageContent.newBuilder().setUid(uid)
                        .setType(MessageType.TEXT)
                        .setContent("hello world").build();
                    UpDownMessage upDownMessage = UpDownMessage.newBuilder().setCid(11)
                        .setFromUid(uid)
                        .setTargetUid(toUid)
                        .setConverType(ConverType.SINGLE)
                        .setContent(content).build();
                    TimMessage timMessage = TimMessage.newBuilder().setType(Type.UpDownMessage)
                        .setUpDownMessage(upDownMessage).build();
                    channelHandlerContext.writeAndFlush(timMessage);
                }
                Thread.sleep(2000);
                ConverReq converReq = ConverReq.newBuilder().setId(222).setType(OperationType.ALL)
                    .build();
                TimMessage timMessage = TimMessage.newBuilder().setType(Type.ConverReq)
                    .setConverReq(converReq).build();
                channelHandlerContext.writeAndFlush(timMessage);
            }
        } else if (message.getType() == Type.MessageAck) {
            MessageAck messageAck = message.getMessageAck();
            log.info("receive message ack:{}", messageAck);
        } else if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upDownMessage = message.getUpDownMessage();
            log.info("receive down message:{}", upDownMessage);
        }
        if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
            log.info("receive heartbeat message:{}", heartBeat);
        }
        if (message.getType() == Type.ConverAck) {
            ConverAck converAck = message.getConverAck();
            log.info("receive conver ack message:{}", converAck);
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
