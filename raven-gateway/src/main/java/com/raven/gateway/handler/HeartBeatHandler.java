package com.raven.gateway.handler;

import com.raven.common.netty.NettyAttrUtil;
import com.raven.common.protos.Message.HeartBeat;
import com.raven.common.protos.Message.HeartBeatType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.raven.common.netty.NettyAttrUtil.*;

@Component
@Sharable
@Slf4j
public class HeartBeatHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) {
        if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
            //response with PONG.
            log.debug("receive heartbeat :{}", JsonHelper.toJsonString(heartBeat));
            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
                HeartBeat heartBeatAck = HeartBeat.newBuilder()
                        .setId(heartBeat.getId())
                        .setHeartBeatType(HeartBeatType.PONG)
                        .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder()
                        .setType(Type.HeartBeat)
                        .setHeartBeat(heartBeatAck)
                        .build();
                ctx.writeAndFlush(ravenMessage);
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
                String appKey = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_APP_KEY);
                String userId = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_USER_ID);
                String deviceId = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_DEVICE_ID);
                log.info("AppKey [{}], User[{}], Device[{}] timeout, NO Ping.  just close it.", appKey, userId, deviceId);
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
