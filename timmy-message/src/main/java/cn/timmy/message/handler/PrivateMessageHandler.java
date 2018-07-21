package cn.timmy.message.handler;

import cn.timmy.common.protos.Message.UpStreamMessage;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.process.PrivateMessageProcessor;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
public class PrivateMessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LogManager.getLogger(
        PrivateMessageHandler.class);

    @Autowired
    private PrivateMessageProcessor privateMessageProcessor;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) {
        if (messageLite instanceof UpStreamMessage) {
            privateMessageProcessor.process(messageLite, channelHandlerContext);
        }
        channelHandlerContext.fireChannelRead(messageLite);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String uid = nettyChannelManager.getUidByChannel(ctx.channel());
        logger.error("caught an ex, channelId:{}, uid:{},ex:{}", ctx.channel().id().asShortText(),
            uid, cause);
        ctx.close();
    }
}

