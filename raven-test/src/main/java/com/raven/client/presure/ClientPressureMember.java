package com.raven.client.presure;

import com.raven.client.common.Utils;
import com.raven.common.protos.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientPressureMember {

    public static volatile int maxTimeDiff = 0;

    public static AtomicLong countTimeDiff = new AtomicLong();

    public static AtomicInteger msgCount = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        maxTimeDiff = 0;
        countTimeDiff.lazySet(0);
        msgCount.lazySet(0);
        log.info("消息总数:{} 总延迟:{} 最大延迟:{}", msgCount.get(), countTimeDiff.get(), maxTimeDiff);
        String token = Utils.getToken("user1", "user1_device");
        for (int i = 2; i < 2501; i++) {
            Thread.sleep(10);
            String uid = "user" + i;
            loginAndSendMessage(uid, token);
        }
    }

    private static void loginAndSendMessage(String uid, String token) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_REUSEADDR, true) //调试用
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IdleStateHandler(10, 10, 15));
                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                    pipeline
                        .addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()));
                    // 对protobuf协议的消息头上加上一个长度为32的整形字段
                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    pipeline.addLast(new ProtobufEncoder());
                    pipeline.addLast(new ClientPressureMemberHandler(uid, token));
                }
            });
        b.connect("35.229.128.80", 7010);
    }

}

