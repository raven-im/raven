package com.tim.single;

import com.tim.common.code.MessageDecoder;
import com.tim.common.code.MessageEncoder;
import com.tim.common.utils.SnowFlake;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Author zxx Description Simple client for module test Date Created on 2018/5/25
 */
@Slf4j
public class Client {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 7270;
//    public static SnowFlake snowFlake = new SnowFlake(1, 2);

    public static void sendSingleMsgTest() throws InterruptedException {
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
        startConnection(b);
    }

    private static void startConnection(Bootstrap b) {
        b.connect(HOST, PORT).addListener(future -> {
            if (future.isSuccess()) {
                //init registry
                log.info("Client connected SingleTcpServer Success...");
            } else {
                log.error("Client connected SingleTcpServer Failed");
            }
        });
        b.connect(HOST, PORT);
    }

}

