package client;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.Auth;
import protobuf.protos.PrivateMessageProto;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx
 * Description 客户端模拟
 * Date Created on 2018/5/25
 */
public class ClientHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private String uid = "";
    private static AtomicLong increased = new AtomicLong(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        uid = Long.toString(increased.getAndIncrement());
        sendLogin(ctx, uid);
    }

    private void sendLogin(ChannelHandlerContext ctx, String uid) {
        Auth.Login.Builder login = Auth.Login.newBuilder();
        login.setToken(uid);
        login.setProtonum(ProtoConstants.LOGIN);
        ByteBuf byteBuf = Utils.pack2Client(login.build());
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg)
            throws Exception {
        if (msg instanceof Auth.Response) {
            Thread.sleep(2000);
            sendPrivateMessage();
        }
    }

    private void sendPrivateMessage() {
        String content = "Hello World!";
        PrivateMessageProto.UpStreamMessageProto.Builder msg = PrivateMessageProto.UpStreamMessageProto
                .newBuilder();
        msg.setContent(content);
        List<String> uids = new ArrayList<>();
        uids.add(String.valueOf((int)(Math.random()*10)%10+1));
        msg.addAllTouid(uids);
        msg.setProtonum(ProtoConstants.UPPRIVATEMESSAGE);
        msg.setSendtime(System.currentTimeMillis());
        ByteBuf byteBuf = Utils.pack2Client(msg.build());
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
}
