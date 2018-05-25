package message.handler;

import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Author zxx
 * Description
 * Date Created on 2018/5/25
 */
public class PrivateMessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
            MessageLite messageLite) throws Exception {

    }
}
