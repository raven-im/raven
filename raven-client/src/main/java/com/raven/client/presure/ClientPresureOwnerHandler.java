package com.raven.client.presure;

import com.raven.client.common.Utils;
import com.raven.client.group.ClientOwner;
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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientPresureOwnerHandler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid;

    private String groupId;

    private String token;

    public HashMap<String, Long> cidTime = new HashMap<String, Long>();

    public ClientPresureOwnerHandler(String uid, String groupId, String token) {
        this.uid = uid;
        this.groupId = groupId;
        this.token = token;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendLogin(ctx, uid);
    }

    private void sendLogin(ChannelHandlerContext ctx, String uid) {
        Login login = Login.newBuilder()
            .setUid(uid)
            .setId(ClientPresureOwner.snowFlake.nextId())
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
        } else if (message.getType() == Type.MessageAck) {
            MessageAck messageAck = message.getMessageAck();
            long time = cidTime.get(String.valueOf(messageAck.getCid()));
            log.info("消息cid:{},延迟:{}ms", messageAck.getCid(), messageAck.getTime() - time);
        } else if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upDownMessage = message.getUpDownMessage();
        } else if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
                long cid = ClientPresureOwner.snowFlake.nextId();
                long time = System.currentTimeMillis();
                cidTime.put(String.valueOf(cid), time);
                MessageContent content = MessageContent.newBuilder()
                    .setUid(uid)
                    .setTime(time)
                    .setType(MessageType.TEXT)
                    .setContent("hello world.")
                    .build();
                UpDownMessage msg = UpDownMessage.newBuilder()
                    .setCid(cid)
                    .setFromUid(uid)
                    .setGroupId(groupId)
                    .setConverType(ConverType.GROUP)
                    .setContent(content)
                    .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
                    .setUpDownMessage(msg).build();
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
