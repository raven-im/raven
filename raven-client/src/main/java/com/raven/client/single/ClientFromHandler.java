package com.raven.client.single;

import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.ConverAck;
import com.raven.common.protos.Message.ConverInfo;
import com.raven.common.protos.Message.ConverReq;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.HeartBeat;
import com.raven.common.protos.Message.HisMessagesAck;
import com.raven.common.protos.Message.HisMessagesReq;
import com.raven.common.protos.Message.Login;
import com.raven.common.protos.Message.LoginAck;
import com.raven.common.protos.Message.MessageAck;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.MessageType;
import com.raven.common.protos.Message.OperationType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientFromHandler extends SimpleChannelInboundHandler<RavenMessage> {

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
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.Login).setLogin(login).build();
        ctx.writeAndFlush(ravenMessage);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RavenMessage message)
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
                    RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
                        .setUpDownMessage(upDownMessage).build();
                    channelHandlerContext.writeAndFlush(ravenMessage);
                }
                Thread.sleep(2000);
                ConverReq converReq = ConverReq.newBuilder().setId(222).setType(OperationType.ALL)
                    .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.ConverReq)
                    .setConverReq(converReq).build();
                channelHandlerContext.writeAndFlush(ravenMessage);
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
            long beginTime = 1L;
            for (ConverInfo converInfo : converAck.getConverListList()) {
                HisMessagesReq hisMessagesReq = HisMessagesReq.newBuilder().setId(333)
                    .setBeaginTime(beginTime).setConverId(converInfo.getConverId()).build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.HisMessagesReq)
                    .setHisMessagesReq(hisMessagesReq).build();
                channelHandlerContext.writeAndFlush(ravenMessage);
            }
        }
        if (message.getType() == Type.HisMessagesAck) {
            HisMessagesAck hisMessagesAck = message.getHisMessagesAck();
            log.info("receive history messaage ack:{}", hisMessagesAck);
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
