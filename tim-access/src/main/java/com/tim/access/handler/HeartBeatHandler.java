package com.tim.access.handler;

import com.google.protobuf.MessageLite;
import com.tim.access.server.AccessTcpServer;
import com.tim.common.netty.ChannelManager;
import com.tim.common.netty.NettyAttrUtil;
import com.tim.common.protos.Message.HeartBeat;
import com.tim.common.protos.Message.HeartBeatType;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Sharable
@Slf4j
public class HeartBeatHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private ChannelManager uidChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) {
        if (messageLite instanceof HeartBeat) {
            HeartBeat heartBeat = (HeartBeat) messageLite;
            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
                HeartBeat heartBeatAck = HeartBeat.newBuilder()
                    .setId(heartBeat.getId())
                    .setHeartBeatType(HeartBeatType.PONG)
                    .build();
                channelHandlerContext.writeAndFlush(heartBeatAck);
            } else if (heartBeat.getHeartBeatType() == HeartBeatType.PONG) {
                NettyAttrUtil
                    .updateReaderTime(channelHandlerContext.channel(), System.currentTimeMillis());
            }
        } else {
            channelHandlerContext.fireChannelRead(messageLite);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                String uid = uidChannelManager.getIdByChannel(ctx.channel());
                HeartBeat heartBeat = HeartBeat.newBuilder()
                    .setId(AccessTcpServer.snowFlake.nextId())
                    .setHeartBeatType(HeartBeatType.PONG)
                    .build();
                ctx.writeAndFlush(heartBeat).addListeners(future -> {
                    if (!future.isSuccess()) {
                        log.info("uid:{} off line", uid);
                        ctx.close();
                    }
                });
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


}
