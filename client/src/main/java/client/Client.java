package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.code.MessageDecoder;
import protobuf.code.MessageEncoder;
import protobuf.utils.ParseRegistryMap;

/**
 * Author zxx Description 客户端 Date Created on 2018/5/25
 */
public class Client {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 7070;
    private static final int clientNum = 10;

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws Exception {
        beginPressTest();
    }

    public static void beginPressTest() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast("MessageDecoder", new MessageDecoder());
                    p.addLast("MessageEncoder", new MessageEncoder());
                    p.addLast(new ClientHandler());
                }
            });
        // Start the client
        ParseRegistryMap.initRegistry();
        for (int i = 1; i <= clientNum; i++) {
            startConnection(b, i);
        }
    }

    private static void startConnection(Bootstrap b, int index) {
        b.connect(HOST, PORT).addListener(future -> {
            if (future.isSuccess()) {
                //init registry
                logger.info("Client:{} connected MessageServer Successed...", index);
            } else {
                logger.error("Client:{} connected MessageServer Failed", index);
            }
        });
    }

}

