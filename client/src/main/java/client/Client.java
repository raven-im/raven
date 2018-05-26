package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.utils.ParseRegistryMap;
import protobuf.code.MessageDecoder;
import protobuf.code.MessageEncoder;

/**
 * update by Damon on 2018年5月12日
 * Simple client for module test
 */
public class Client {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "9090"));
    public static final int clientNum = Integer.parseInt(System.getProperty("size", "10"));
    public static final int frequency = 100;  //ms

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
        // Start the client.
        for (int i = 1; i <= clientNum; i++) {
            startConnection(b, i);
        }
    }

    private static void startConnection(Bootstrap b, int index) {
        b.connect(HOST, PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future)
                    throws Exception {
                if (future.isSuccess()) {
                    //init registry
                    ParseRegistryMap.initRegistry();
                    logger.info("Client[{}] connected Gate Successed...", index);
                } else {
                    logger.error("Client[{}] connected Gate Failed", index);
                }
            }
        });
    }
}

