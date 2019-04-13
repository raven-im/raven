package com.tim.access.handler.client;

import com.google.protobuf.MessageLite;
import com.tim.access.config.S2sChannelManager;
import com.tim.access.util.IpUtil;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.protos.Auth.ServerInfo;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.utils.SnowFlake;
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
public class S2sClientHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private S2sChannelManager s2sChannelManager;

    @Value("${netty.server.port}")
    private int nettyServerPort;

    @Autowired
    private SnowFlake snowFlake;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("server connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
        ServerInfo serverInfo = ServerInfo.newBuilder()
            .setId(snowFlake.nextId())
            .setIp(IpUtil.getIp())
            .setPort(nettyServerPort).build();
        ctx.writeAndFlush(serverInfo);
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
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite msg) throws Exception {
        if (msg instanceof MessageAck) {
            MessageAck ack = (MessageAck) msg;
            List<Channel> channels = uidChannelManager.getChannelsById(ack.getTargetUid());
            // TODO channels为空
            channels.forEach(channel -> channel.writeAndFlush(ack));
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}

