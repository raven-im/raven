package com.tim.server.server;

import com.tim.common.code.MessageDecoder;
import com.tim.common.code.MessageEncoder;
import com.tim.common.utils.SnowFlake;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * Author zxx Description 消息服务 Date Created on 2018/5/25
 */
@Slf4j
public class BaseTcpMessageServer {

    @Value("${netty.server.port}")
    private int nettyServerPort;

    public static SnowFlake snowFlake;

    private ConcurrentMap<String, ChannelHandler> specificHandler = new ConcurrentHashMap<>();

    protected void startServer(long dcId, long machineId) {
        startMessageServer();
        snowFlake = new SnowFlake(dcId, machineId);
    }

    private void startMessageServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel)
                    throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("IdleStateHandler",
                        new IdleStateHandler(25, 0, 0, TimeUnit.SECONDS));
                    pipeline.addLast("MessageDecoder", new MessageDecoder());
                    pipeline.addLast("MessageEncoder", new MessageEncoder());
                    specificHandler.keySet().forEach(key -> pipeline.addLast(key, specificHandler.get(key)));
                }
            });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(nettyServerPort)).addListener(future -> {
            if (future.isSuccess()) {
                log.info("MessageServer Started Success,port:{}", nettyServerPort);
            } else {
                log.error("MessageServer Started Failed!");
            }
        });
    }

    private void bindConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_LINGER, 0);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //调试用
    }

    protected void addHandler(String key, ChannelHandler handler) {
        specificHandler.putIfAbsent(key, handler);
    }
}
