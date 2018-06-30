package cn.timmy.message.handler;

import cn.timmy.common.protos.Auth.Login;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.process.LoginAuthProcessor;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 登录验证、绑定连接
 * Date Created on 2018/5/25
 */
@Component
@Sharable
public class LoginAuthHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LogManager.getLogger(
        LoginAuthHandler.class);

    @Autowired
    private LoginAuthProcessor loginAuthProcessor;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof Login) {
            loginAuthProcessor.process(messageLite, channelHandlerContext);
        }
        channelHandlerContext.fireChannelRead(messageLite);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = nettyChannelManager.getUidByChannel(ctx.channel());
        logger.info("client disconnected channelId:{},uid:{}", ctx.channel().id().asShortText(),
            uid);
        nettyChannelManager.removeChannel(ctx.channel());
    }
}

