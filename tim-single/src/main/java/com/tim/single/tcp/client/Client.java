package com.tim.single.tcp.client;

import com.tim.common.code.MessageDecoder;
import com.tim.common.code.MessageEncoder;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class Client {

    private String host;
    private int port;

    public void sendSingleMsg(UpDownMessage msg) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                    .addLast(new ProtobufVarint32FrameDecoder())// 处理半包消息的解码类
                    .addLast("MessageDecoder", new MessageDecoder())
                    .addLast(new ProtobufVarint32LengthFieldPrepender())// 对protobuf协议的消息头上加上一个长度为32的整形字段
                    .addLast("MessageEncoder", new MessageEncoder())
                    .addLast(new ClientHandler(msg));
                }
            });
        startConnection(b);
    }

    private void startConnection(Bootstrap b) {
        b.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                //init registry
                log.info("Client connected Access Server {}:{} Success...", host, port);
            } else {
                log.error("Client connected Access Server {}:{} Failed", host, port);
            }
        });
    }
}

