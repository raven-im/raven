package com.raven.route.message.netty;

import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.HeartBeat;
import com.raven.common.protos.Message.HeartBeatType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private ServerChannelManager internalServerChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message)
        throws Exception {
        if (message.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = message.getHeartBeat();
            log.info("receive hearbeat :{}", heartBeat);
            if (heartBeat.getHeartBeatType() == HeartBeatType.PING) {
                HeartBeat heartBeatAck = HeartBeat.newBuilder()
                    .setId(heartBeat.getId())
                    .setHeartBeatType(HeartBeatType.PONG)
                    .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.HeartBeat)
                    .setHeartBeat(heartBeatAck).build();
                ctx.writeAndFlush(ravenMessage);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("raven-gateway server disconnected address:{}", ctx.channel().remoteAddress());
        GatewayServerInfo server = internalServerChannelManager.getServerByChannel(ctx.channel());
        internalServerChannelManager.removeServer(server);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

}
