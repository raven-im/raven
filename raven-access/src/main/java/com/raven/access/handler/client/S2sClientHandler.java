package com.raven.access.handler.client;

import com.raven.access.config.S2sChannelManager;
import com.raven.access.util.IpUtil;
import com.raven.common.loadbalance.Server;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.MessageAck;
import com.raven.common.protos.Message.ServerInfo;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.SnowFlake;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class S2sClientHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private S2sChannelManager s2sChannelManager;

    @Value("${netty.tcp.port}")
    private int nettyTcpPort;

    @Autowired
    private SnowFlake snowFlake;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("server connected remote address:{}", ctx.channel().remoteAddress());
        ServerInfo serverInfo = ServerInfo.newBuilder()
            .setId(snowFlake.nextId())
            .setIp(IpUtil.getIp())
            .setPort(nettyTcpPort).build();
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.ServerInfo)
            .setServerInfo(serverInfo).build();
        ctx.writeAndFlush(ravenMessage);
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
        // TODO 是否重连
        Server server = s2sChannelManager.getServerByChannel(ctx.channel());
        s2sChannelManager.removeServer(server);
        log.info(server + " disconnect");
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage msg) throws Exception {
        if (msg.getType() == Type.MessageAck) {
            MessageAck ack = msg.getMessageAck();
            log.info("receive down message ack:{}",ack);
            List<Channel> channels = uidChannelManager.getChannelsById(ack.getTargetUid());
            // TODO channels为空
            RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.MessageAck)
                .setMessageAck(ack).build();
            channels.forEach(channel -> channel.writeAndFlush(ravenMessage));
        } else if (msg.getType() == Type.UpDownMessage) {
            UpDownMessage downMessage = msg.getUpDownMessage();
            log.info("receive down message:{}",downMessage);
            List<Channel> channels = uidChannelManager
                .getChannelsById(downMessage.getTargetUid());
            RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
                .setUpDownMessage(downMessage).build();
            channels.forEach(channel -> channel.writeAndFlush(ravenMessage));
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}

