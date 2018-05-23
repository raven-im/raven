package message.handler;

import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Dell on 2016/2/18.
 */


public class MessageServerHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LoggerFactory.getLogger(MessageServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO 保存channel id
        logger.info("channel id:{}",ctx.channel().id());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
            MessageLite messageLite) throws Exception {
        // TODO 保存uid映射connection

    }
}

