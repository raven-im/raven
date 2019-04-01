package com.tim.group.tcp.server;

import com.tim.common.code.MessageDecoder;
import com.tim.common.code.MessageEncoder;
import com.tim.common.utils.SnowFlake;
import com.tim.group.tcp.handler.HeartBeatHandler;
import com.tim.group.tcp.handler.LoginAuthHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Author zxx Description 消息服务 Date Created on 2018/5/25
 */
@Component
@Slf4j
public class GroupTcpMessageServer {

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    @Autowired
    private LoginAuthHandler loginAuthHandler;

    @Value("${node.data-center-id}")
    private int dataCenterId;

    @Value("${node.machine-id}")
    private int machineId;


    @Value("${netty.server.port}")
    private int nettyServerPort;

    public static SnowFlake snowFlake;

    @PostConstruct
    public void startServer() {
        startMessageServer();
        snowFlake = new SnowFlake(dataCenterId, machineId);
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
                    pipeline.addLast("HeartBeatHandler", heartBeatHandler);
                    pipeline.addLast("MessageServerHandler", loginAuthHandler);
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
}
