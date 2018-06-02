package message.handler;

import com.google.protobuf.MessageLite;
import common.connection.ChannelManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.MessageStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.Auth.Login;
import protobuf.protos.Auth.Response;
import protobuf.protos.ResponseEnum;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx Description 登录验证、绑定连接 Date Created on 2018/5/25
 */
public class BindUserHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LoggerFactory.getLogger(BindUserHandler.class);

    private final ChannelManager connectionManager;

    public BindUserHandler(ChannelManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channelId映射connection
        logger.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        // uid映射connection
        if (messageLite instanceof Login) {
            String token = ((Login) messageLite).getToken();
            // todo 校验token
            connectionManager.addUid2Channel(token, channelHandlerContext.channel());
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
        String uid = connectionManager.getUidByChannel(ctx.channel());
        logger.error("caught an ex, channelId:{}, uid:{},ex:{}", ctx.channel().id().asShortText(),
            uid, cause);
        ctx.close();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = connectionManager.getUidByChannel(ctx.channel());
        logger.info("client disconnected channelId:{},uid:{}", ctx.channel().id().asShortText(),
            uid);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        connectionManager.removeChannel(ctx.channel());
        super.handlerRemoved(ctx);
    }
}

