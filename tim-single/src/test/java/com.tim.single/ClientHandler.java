package com.tim.single;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Auth;
import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Common;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Common.MessageContent;
import com.tim.common.protos.Common.MessageType;
import com.tim.common.protos.Message;
import com.tim.common.protos.Message.Direction;
import com.tim.common.protos.Message.HeartBeat;
import com.tim.common.protos.Message.HeartBeatType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class ClientHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private String fromUserId = "user1";
    private String targetUserId = "user2";
    private String convId = "user1_user2";

//    private static AtomicLong increased = new AtomicLong(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
//        uid = Long.toString(increased.getAndIncrement());
        sendPrivateMessage();
//        sendLogin(ctx, uid);
    }

    private void sendLogin(ChannelHandlerContext ctx, String uid) {
        Login login = Login.newBuilder()
            .setUid(uid)
            .build();
        ByteBuf byteBuf = Utils.pack2Client(login);
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg)
        throws Exception {
//        if (msg instanceof Auth.LoginAck) {
//            Thread.sleep(2000);
//            sendPrivateMessage();
//            Timer timer = new Timer();
//            timer.schedule(new HeartBeatTask(), 20 * 1000, 20 * 1000);
//        }
    }

    private void sendPrivateMessage() {
//        String content = "Hello World!";
//        List<String> uids = new ArrayList<>();
//        uids.add(String.valueOf((int) (Math.random() * 10) % 10 + 1));
        Common.MessageContent content = Common.MessageContent.newBuilder()
            .setId(1)
            .setUid(fromUserId)
            .setTime(System.currentTimeMillis())
            .setType(MessageType.TEXT)
            .setContent("Hello world!")
            .build();

        Message.UpDownMessage msg = Message.UpDownMessage.newBuilder()
            .setId(100)
//            .setClientId(1000)
            .setFromId(fromUserId)
            .setTargetId(targetUserId)
            .setConversationType(ConversationType.SINGLE)
            .setConversasionId(convId)
            .setContent(content)
            .setDirection(Direction.SS)
            .build();
        ByteBuf byteBuf = Utils.pack2Client(msg);
        messageConnectionCtx.writeAndFlush(byteBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    class HeartBeatTask extends TimerTask {

        @Override
        public void run() {
            HeartBeat beat = HeartBeat.newBuilder().setHeartBeatType(HeartBeatType.PING).build();
            ByteBuf byteBuf = Utils.pack2Client(beat);
            messageConnectionCtx.channel().writeAndFlush(byteBuf);
        }
    }
}
