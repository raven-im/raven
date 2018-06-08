package message.handler;

import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import message.common.ChannelManager;
import message.process.LoginAuthProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.Auth.Login;

/**
 * Author zxx
 * Description 登录验证、绑定连接
 * Date Created on 2018/5/25
 */
public class LoginAuthHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LoggerFactory.getLogger(LoginAuthHandler.class);

    private final ChannelManager connectionManager;

    public LoginAuthHandler(ChannelManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof Login) {
            LoginAuthProcessor.getInstance().process(messageLite, channelHandlerContext);
        }
        channelHandlerContext.fireChannelRead(messageLite);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String uid = connectionManager.getUidByChannel(ctx.channel());
        logger.error("caught an ex, channelId:{}, uid:{},ex:{}", ctx.channel().id().asShortText(),
            uid, cause);
        connectionManager.removeChannel(ctx.channel());
        ctx.close();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = connectionManager.getUidByChannel(ctx.channel());
        logger.info("client disconnected channelId:{},uid:{}", ctx.channel().id().asShortText(),
            uid);
        connectionManager.removeChannel(ctx.channel());
    }

    // 心跳
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                String uid = connectionManager.getUidByChannel(ctx.channel());
                logger.info("uid:{} read idle", uid);
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

