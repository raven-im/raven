package com.tim.client;

import com.tim.common.protos.Message.Code;
import com.tim.common.protos.Message.ConverType;
import com.tim.common.protos.Message.Login;
import com.tim.common.protos.Message.LoginAck;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.MessageContent;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<TimMessage> {

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
                int i = 0;
                while (i < 10) {
                    Thread.sleep(1000);
                    MessageContent content = MessageContent.newBuilder().setUid(uid)
                        .setContent("hello world").build();
                    UpDownMessage upDownMessage = UpDownMessage.newBuilder().setCid(11)
                        .setFromUid(uid)
                        .setTargetUid(uid).setConverType(
                            ConverType.SINGLE).setContent(content).build();
                    TimMessage timMessage = TimMessage.newBuilder().setType(Type.UpDownMessage)
                        .setUpDownMessage(upDownMessage).build();
                    channelHandlerContext.writeAndFlush(timMessage);
                    i++;
                }
            }
        }
        if (message.getType() == Type.MessageAck) {
            MessageAck messageAck = message.getMessageAck();
            log.info("receive message ack:{}", messageAck);
        }
        if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upDownMessage = message.getUpDownMessage();
            log.info("receive down message:{}", upDownMessage);
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
