package com.tim.group.tcp.handler;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Auth.Login;
import com.tim.group.tcp.channel.NettyChannelManager;
import com.tim.group.tcp.process.LoginAuthProcessor;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class LoginAuthHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private LoginAuthProcessor loginAuthProcessor;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
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
        log.info("client disconnected channelId:{},uid:{}", ctx.channel().id().asShortText(),
            uid);
        nettyChannelManager.removeChannel(ctx.channel());
    }
}

