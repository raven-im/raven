package message.handler;

import com.google.protobuf.MessageLite;
import common.connection.Connection;
import common.connection.ConnectionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.MessageStarter;
import message.utils.NettyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.Auth.Login;
import protobuf.protos.Auth.Response;
import protobuf.protos.ResponseEnum;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx
 * Description 登录验证、绑定连接
 * Date Created on 2018/5/25
 */
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
                ctx.channel().id().asShortText());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel());
        connectionManager.addConnection(connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
            MessageLite messageLite) throws Exception {
        // uid映射connection
        if (messageLite instanceof Login) {
            String uid = ((Login) messageLite).getToken();
            // todo 校验uid
            connectionManager.addUid2Connection(uid, channelHandlerContext.channel());
            Response response = Response.newBuilder()
                    .setCode(ResponseEnum.SUCCESS.code)
                    .setMsg(ResponseEnum.SUCCESS.msg)
                    .setProtonum(ProtoConstants.RESPONSE)
                    .setMsgid(MessageStarter.SnowFlake.nextId())
                    .build();
            channelHandlerContext.channel().writeAndFlush(response);
        }
        channelHandlerContext.fireChannelRead(messageLite);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.getConnection(ctx.channel());
        logger.error("caught an ex, channelId:{}, uid:{},ex:{}", ctx.channel().id().asShortText(), connection.getUid(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        logger.info("client disconnected channelId:{},uid:{}", ctx.channel().id().asShortText(),connection.getUid());
    }
}

