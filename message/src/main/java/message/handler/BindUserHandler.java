package message.handler;

import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.utils.Connection;
import message.utils.ConnectionManager;
import message.utils.NettyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.Auth;

public class BindUserHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LoggerFactory.getLogger(BindUserHandler.class);

    private final ConnectionManager connectionManager;

    public BindUserHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channelId映射connection
        logger.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
                ctx.channel().id());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel());
        connectionManager.add(connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
            MessageLite messageLite) throws Exception {
        // uid映射connection
        if (messageLite instanceof Auth.Login) {
            String uid = ((Auth.Login) messageLite).getToken();
            connectionManager.addUid(uid, channelHandlerContext.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        logger.error("caught an ex, channel={}, conn={}", ctx.channel(), connection, cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        logger.info("client disconnected conn={}", connection);
    }
}

