package cn.timmy.message.handler;

import cn.timmy.common.protos.Ack.AckMessage;
import cn.timmy.message.channel.NettyChannelManager;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class AckMesaageHandler  extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LogManager.getLogger(
        LoginAuthHandler.class);

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
        if (messageLite instanceof AckMessage) {

        }
        channelHandlerContext.fireChannelRead(messageLite);
    }

}
