package cn.timmy.message.handler;

import cn.timmy.common.protos.MessageProto.UpStreamMessageProto;
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

/**
 * Author zxx
 * Description 私聊消息处理
 * 单聊聊消息handler Date Created on 2018/5/25
 */
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
        if (messageLite instanceof UpStreamMessageProto) {
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
