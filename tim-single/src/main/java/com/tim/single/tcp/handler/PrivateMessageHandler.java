package com.tim.single.tcp.handler;

import com.tim.single.tcp.channel.NettyChannelManager;
import com.tim.single.tcp.process.PrivateMessageProcessor;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class PrivateMessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private PrivateMessageProcessor privateMessageProcessor;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) {
        channelHandlerContext.fireChannelRead(messageLite);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String uid = nettyChannelManager.getUidByChannel(ctx.channel());
        log.error("caught an ex, channelId:{}, uid:{},ex:{}", ctx.channel().id().asShortText(),
            uid, cause);
        ctx.close();
    }
}

