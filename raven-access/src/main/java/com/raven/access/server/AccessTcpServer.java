package com.raven.access.server;

import com.raven.access.handler.server.ConversationHandler;
import com.raven.access.handler.server.HeartBeatHandler;
import com.raven.access.handler.server.HistoryHandler;
import com.raven.access.handler.server.LoginAuthHandler;
import com.raven.access.handler.server.MesaageHandler;
import com.raven.common.loadbalance.Server;
import com.raven.common.protos.Message;
import com.raven.common.utils.IpUtil;
import com.raven.storage.route.RouteManager;
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
import java.net.InetSocketAddress;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccessTcpServer {

    @Value("${netty.tcp.port}")
    private int nettyTcpPort;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    @Autowired
    private LoginAuthHandler loginAuthHandler;

    @Autowired
    private MesaageHandler mesaageHandler;

    @Autowired
    private ConversationHandler conversationHandler;

    @Autowired
    private HistoryHandler historyHandler;

    @Autowired
    private RouteManager routeManager;

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
                protected void initChannel(SocketChannel channel)
                    throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new IdleStateHandler(10, 10, 15));
                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                    pipeline.addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()));
                    // 对protobuf协议的消息头上加上一个长度为32的整形字段
                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    pipeline.addLast(new ProtobufEncoder());
                    pipeline.addLast("LoginAuthHandler", loginAuthHandler);
                    pipeline.addLast("HeartBeatHandler", heartBeatHandler);
                    pipeline.addLast("MesaageHandler", mesaageHandler);
                    pipeline.addLast("ConversationHandler", conversationHandler);
                    pipeline.addLast("HistoryHandler", historyHandler);
                }
            });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(nettyTcpPort)).addListener(future -> {
            if (future.isSuccess()) {
                log.info("raven-access tcp server start success on port:{}", nettyTcpPort);
            } else {
                log.error("raven-access tcp server start failed!");
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
        routeManager.serverDown(getLocalServer());
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close raven-access tcp server success");
    }

    private Server getLocalServer() {
        return new Server(IpUtil.getIp(), nettyTcpPort);
    }
}
