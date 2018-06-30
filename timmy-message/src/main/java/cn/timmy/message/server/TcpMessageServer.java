package cn.timmy.message.server;

import cn.timmy.common.code.MessageDecoder;
import cn.timmy.common.code.MessageEncoder;
import cn.timmy.common.utils.ParseRegistryMap;
import cn.timmy.common.utils.SnowFlake;
import cn.timmy.message.handler.HeartBeatHandler;
import cn.timmy.message.handler.LoginAuthHandler;
import cn.timmy.message.handler.PrivateMessageHandler;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 消息服务
 * Date Created on 2018/5/25
 */
@Component
@Order(1)
public class TcpMessageServer {

    private static final Logger logger = LogManager.getLogger(TcpMessageServer.class);

    @Value("${netty.server.port}")
    private int nettyServerPort;

    public static SnowFlake snowFlake;

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    @Autowired
    private LoginAuthHandler loginAuthHandler;

    @Autowired
    private PrivateMessageHandler privateMessageHandler;

    @PostConstruct
    public void startServer() {
        startMessageServer();
        snowFlake = new SnowFlake(1, 1);
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
                    pipeline.addLast("PrivateMessageHandler", privateMessageHandler);
                }
            });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(nettyServerPort)).addListener(future -> {
            if (future.isSuccess()) {
                ParseRegistryMap.initRegistry();
                logger.info("MeaageServer Started Success,port:{}", nettyServerPort);
            } else {
                logger.error("MeaageServer Started Failed!");
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
