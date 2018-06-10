package message;

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
import message.handler.LoginAuthHandler;
import message.handler.PrivateMessageHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protobuf.code.MessageDecoder;
import protobuf.code.MessageEncoder;
import protobuf.utils.ParseRegistryMap;

/**
 * Author zxx
 * Description 消息服务
 * Date Created on 2018/5/25
 */
public class MessageServer {

    private static final Logger logger = LogManager.getLogger(MessageServer.class);

    public static void startMessageServer(int port) {
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
                    pipeline.addLast("MessageServerHandler", new LoginAuthHandler());
                    pipeline.addLast("PrivateMessageHandler", new PrivateMessageHandler());
                }
            });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(port)).addListener(future -> {
            if (future.isSuccess()) {
                ParseRegistryMap.initRegistry();
                logger.info("MeaageServer Started Success,port:{}", port);
            } else {
                logger.error("MeaageServer Started Failed!");
            }
        });
    }

    private static void bindConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_LINGER, 0);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //调试用
    }

}
