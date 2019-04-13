package com.tim.access.handler.server;

import com.google.protobuf.MessageLite;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.netty.NettyAttrUtil;
import com.tim.common.protos.Message.HeartBeat;
import com.tim.common.protos.Message.HeartBeatType;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
import com.tim.common.utils.SnowFlake;
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
public class HeartBeatHandler extends SimpleChannelInboundHandler<TimMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        TimMessage message) {
        if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
                HeartBeat heartBeatAck = HeartBeat.newBuilder()
                    .setId(heartBeat.getId())
                    .setHeartBeatType(HeartBeatType.PONG)
                    .build();
                TimMessage timMessage = TimMessage.newBuilder().setType(Type.HeartBeat)
                    .setHeartBeat(heartBeatAck).build();
                channelHandlerContext.writeAndFlush(timMessage);
            } else if (heartBeat.getHeartBeatType() == HeartBeatType.PONG) {
                log.info("heartBeat msg:{}", heartBeat.toString());
                NettyAttrUtil
                    .updateReaderTime(channelHandlerContext.channel(), System.currentTimeMillis());
            }
        } else {
            channelHandlerContext.fireChannelRead(message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                String uid = uidChannelManager.getIdByChannel(ctx.channel());
                HeartBeat heartBeat = HeartBeat.newBuilder()
                    .setId(snowFlake.nextId())
                    .setHeartBeatType(HeartBeatType.PING)
                    .build();
                TimMessage timMessage = TimMessage.newBuilder().setHeartBeat(heartBeat)
                    .setType(Type.HeartBeat).build();
                ctx.writeAndFlush(timMessage).addListeners(future -> {
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
