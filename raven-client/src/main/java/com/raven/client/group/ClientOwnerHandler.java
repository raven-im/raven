package com.raven.client.group;

import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.ConverType;
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
public class ClientOwnerHandler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid;
    private String groupId;

    public ClientOwnerHandler(String uid, String groupId) {
        this.uid = uid;
        this.groupId = groupId;
    }

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
                MessageContent content = MessageContent.newBuilder()
                    .setId(88)
                    .setUid(uid)
                    .setTime(System.currentTimeMillis())
                    .setType(MessageType.TEXT)
                    .setContent("hello world.")
                    .build();

                UpDownMessage msg = UpDownMessage.newBuilder()
                    .setCid(90)
                    .setFromUid(uid)
                    .setTargetUid("invitee2")
                    .setGroupId(groupId)
                    .setConverType(ConverType.GROUP)
                    .setContent(content)
                    .build();

                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
                    .setUpDownMessage(msg).build();
                channelHandlerContext.writeAndFlush(ravenMessage);
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
