package com.raven.gateway.handler;

import com.raven.common.netty.IdChannelManager;
import com.raven.common.netty.NettyAttrUtil;
import com.raven.common.protos.Message.HeartBeat;
import com.raven.common.protos.Message.HeartBeatType;
import com.raven.common.protos.Message.NotifyMessage;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.SnowFlake;
import com.raven.storage.conver.ConverManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class MessageRouteHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private ConverManager converManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("server connected remote address:{}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("server disconnect remote address:{}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage msg) throws Exception {
        NettyAttrUtil.updateReaderTime(ctx.channel(), System.currentTimeMillis());
        if (msg.getType() == Type.HeartBeat) {
            HeartBeat heartBeat = msg.getHeartBeat();
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
        } else if (msg.getType() == Type.UpDownMessage) {
            UpDownMessage downMessage = msg.getUpDownMessage();
            log.info("receive down message:{}", JsonHelper.toJsonString(downMessage));
            converManager.saveWaitUserAckMsg(downMessage.getTargetUid(), downMessage.getConverId(),
                downMessage.getId());
            List<Channel> channels = uidChannelManager.getChannelsById(downMessage.getTargetUid());
            RavenMessage ravenMessage = RavenMessage.newBuilder()
                    .setType(Type.UpDownMessage)
                    .setUpDownMessage(downMessage)
                    .build();
            for (Channel channel : channels) {
                channel.writeAndFlush(ravenMessage).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("push msg to uid:{} fail", downMessage.getTargetUid());
                        channel.close();
                    }
                });
            }
        } else if (msg.getType() == Type.NotifyMessage) {
            NotifyMessage notification = msg.getNotifyMessage();
            log.info("receive down notification:{}", JsonHelper.toJsonString(notification));
            List<Channel> channels = uidChannelManager.getChannelsById(notification.getTargetUid());
            for (Channel channel : channels) {
                channel.writeAndFlush(msg).addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("push notification to uid:{} fail", notification.getTargetUid());
                        channel.close();
                    }
                });
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                Long lastReadTime = NettyAttrUtil.getReaderTime(ctx.channel());
                if (null != lastReadTime && System.currentTimeMillis() - lastReadTime > 30000) {
                    log.info("server:{} last read time more than 30 seconds",
                        ctx.channel().remoteAddress());
                    ctx.close();
                    return;
                }
                HeartBeat heartBeat = HeartBeat.newBuilder()
                    .setId(snowFlake.nextId())
                    .setHeartBeatType(HeartBeatType.PING)
                    .build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setHeartBeat(heartBeat)
                    .setType(Type.HeartBeat).build();
                ctx.writeAndFlush(ravenMessage).addListeners(future -> {
                    if (!future.isSuccess()) {
                        log.info("server:{} off line", ctx.channel().remoteAddress());
                        ctx.close();
                    }
                });
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

