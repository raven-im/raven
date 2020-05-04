package com.raven.client.presure;

import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.Login;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.MessageType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;

import static com.raven.client.common.Utils.rspHeartBeat;
import static com.raven.client.common.Utils.sendLogin;

@Slf4j
public class ClientPressureOwnerHandler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid;

    private String groupId;

    private String token;

    public HashMap<String, Long> cidTime = new HashMap<String, Long>();

    public ClientPressureOwnerHandler(String uid, String groupId, String token) {
        this.uid = uid;
        this.groupId = groupId;
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
        if (message.getType() == Type.HeartBeat) {
            rspHeartBeat(ctx, message.getHeartBeat());
        }
        //TODO ??
        while (true) {
            sendMessage(ctx);
            Thread.sleep(357);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

    private void sendMessage(ChannelHandlerContext ctx) {
        long cid = ClientPressureOwner.snowFlake.nextId();
        long time = System.currentTimeMillis();
        MessageContent content = MessageContent.newBuilder()
            .setTime(time)
            .setType(MessageType.TEXT)
            .setContent("hello world.")
            .build();
        UpDownMessage msg = UpDownMessage.newBuilder()
            .setCid(cid)
            .setFromUid(uid)
            .setTargetUid(groupId)
            .setConverType(ConverType.GROUP)
            .setContent(content)
            .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
            .setUpDownMessage(msg).build();
        ctx.writeAndFlush(ravenMessage);
    }

}
