package client;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import protobuf.protos.Auth;
import protobuf.protos.Auth.Login;
import protobuf.protos.HeartBeat.Beat;
import protobuf.protos.MessageProto.UpStreamMessageProto;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx Description 客户端模拟 Date Created on 2018/5/25
 */
public class ClientHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid = "";
    private static AtomicLong increased = new AtomicLong(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        uid = Long.toString(increased.getAndIncrement());
        sendLogin(ctx, uid);
    }

    private void sendLogin(ChannelHandlerContext ctx, String uid) {
        Login login = Login.newBuilder()
            .setToken(uid)
            .setProtonum(ProtoConstants.LOGIN)
            .build();
        ByteBuf byteBuf = Utils.pack2Client(login);
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg)
        throws Exception {
        if (msg instanceof Auth.Response) {
            Thread.sleep(2000);
            sendPrivateMessage();
            Timer timer = new Timer();
            timer.schedule(new HeartBeatTask(), 20 * 1000, 20 * 1000);
        }
    }

    private void sendPrivateMessage() {
        String content = "Hello World!";
        List<String> uids = new ArrayList<>();
        uids.add(String.valueOf((int) (Math.random() * 10) % 10 + 1));
        UpStreamMessageProto msg = UpStreamMessageProto
            .newBuilder()
            .setContent(content)
            .addAllTouid(uids)
            .setProtonum(ProtoConstants.UPPRIVATEMESSAGE)
            .setSendtime(System.currentTimeMillis())
            .build();
        ByteBuf byteBuf = Utils.pack2Client(msg);
        messageConnectionCtx.channel().writeAndFlush(byteBuf);
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
            Beat beat = Beat.newBuilder().setHeartbeat("HeartBeat").build();
            ByteBuf byteBuf = Utils.pack2Client(beat);
            messageConnectionCtx.channel().writeAndFlush(byteBuf);
        }
    }
}
