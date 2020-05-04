package com.raven.client.single;

import com.raven.common.protos.Message.*;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.raven.client.common.Utils.*;

@Slf4j
public class ClientFromHandler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid;
    private String token;

    private String[] toUidList = {"test1"};

    public ClientFromHandler(String uid, String token) {
        super(true);
        this.uid = uid;
        this.token = token;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendLogin(ctx, token);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message)
            throws Exception {
        if (message.getType() == Type.LoginAck) {
            LoginAck loginAck = message.getLoginAck();
            log.info("login ack:{}", JsonHelper.toJsonString(loginAck));
            if (loginAck.getCode() == Code.SUCCESS) {
                sendHeartBeat(ctx);
                for (String toUid : toUidList) {
                    Thread.sleep(1000);
                    MessageContent content = MessageContent.newBuilder()
                            .setType(MessageType.TEXT)
                            .setContent("hello world")
                            .build();
                    long cid = snowFlake.nextId();
                    UpDownMessage upDownMessage = UpDownMessage.newBuilder()
                            .setCid(cid)
                            .setFromUid(uid)
                            .setTargetUid(toUid)
                            .setConverType(ConverType.SINGLE)
                            .setContent(content)
                            .build();
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                            .setType(Type.UpDownMessage)
                            .setUpDownMessage(upDownMessage)
                            .build();
                    log.info("send message :{}", JsonHelper.toJsonString(upDownMessage));
                    ctx.writeAndFlush(ravenMessage);
                }
            }
        }
        if (message.getType() == Type.MessageAck) {
            MessageAck messageAck = message.getMessageAck();
            log.info("receive message ack:{}", JsonHelper.toJsonString(messageAck));
        }
        if (message.getType() == Type.HeartBeat) {
            rspHeartBeat(ctx, message.getHeartBeat());
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
