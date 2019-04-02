package com.tim.single.tcp.handler;

import com.google.protobuf.MessageLite;
import com.tim.single.tcp.channel.NettyChannelManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class HeartBeatHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite){
        channelHandlerContext.fireChannelRead(messageLite);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                String uid = nettyChannelManager.getUidByChannel(ctx.channel());
                log.info("uid:{} read idle", uid);
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
