package com.raven.group.tcp.server;

import com.raven.common.protos.Message;
import com.raven.group.tcp.handler.MessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GroupTcpServer {

    @Value("${netty.tcp.port}")
    private int nettyTcpPort;

    @Autowired
    private MessageHandler messageHandler;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    @PostConstruct
    public void startServer() {
        startMessageServer();
    }

    private void startMessageServer() {
        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(new IdleStateHandler(10, 10, 15))
                        .addLast(new ProtobufVarint32FrameDecoder())// 处理半包消息的解码类
                        .addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()))
                        .addLast(new ProtobufVarint32LengthFieldPrepender())// 对protobuf协议的消息头上加上一个长度为32的整形字段
                        .addLast(new ProtobufEncoder())
                        .addLast("MessageHandler", messageHandler);
                }
            });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(nettyTcpPort)).addListener(future -> {
            if (future.isSuccess()) {
                log.info("raven group server start success on port:{}", nettyTcpPort);
            } else {
                log.error("raven group server start failed!");
            }
        });
    }

    private void bindConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //调试用
    }

    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close raven group server success");
    }
}
