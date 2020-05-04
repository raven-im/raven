package com.raven.gateway.server;

import com.raven.common.protos.Message;
import com.raven.gateway.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

import static com.raven.common.utils.Constants.HEART_BEAT_DETECT;

@Component
@Slf4j
public class TcpServer {

    @Value("${netty.tcp.port}")
    private int tcpPort;

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    private EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2);

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    @Autowired
    private AuthenticationHandler authenticationHandler;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private ConversationHandler conversationHandler;

    @Autowired
    private HistoryHandler historyHandler;

    @Autowired
    private AckMessageHandler ackMessageHandler;

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
                        ChannelPipeline pipeline = channel.pipeline();
                        // 20s trigger to close channel for no heart beat.
                        pipeline.addLast(new IdleStateHandler(0, 0, HEART_BEAT_DETECT))
                                .addLast(new ProtobufVarint32FrameDecoder())
                                .addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()))
                                // 对protobuf协议的消息头上加上一个长度为32的整形字段
                                .addLast(new ProtobufVarint32LengthFieldPrepender())
                                .addLast(new ProtobufEncoder())
                                .addLast("AuthenticationHandler", authenticationHandler)
                                .addLast("HeartBeatHandler", heartBeatHandler)
                                .addLast(executorGroup, "MessageHandler", messageHandler)
                                .addLast(executorGroup, "AckMessageHandler", ackMessageHandler)
                                .addLast(executorGroup, "ConversationHandler", conversationHandler)
                                .addLast(executorGroup, "HistoryHandler", historyHandler);
                    }
                });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(tcpPort)).addListener(future -> {
            if (future.isSuccess()) {
                log.info("raven-gateway tcp server start success on port:{}", tcpPort);
            } else {
                log.error("raven-gateway tcp server start failed!");
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
        log.info("close raven-gateway tcp server success");
    }
}
