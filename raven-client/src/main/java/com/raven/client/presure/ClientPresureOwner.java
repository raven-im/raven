package com.raven.client.presure;

import com.raven.client.common.Utils;
import com.raven.client.group.bean.GroupOutParam;
import com.raven.common.param.ServerInfoOutParam;
import com.raven.common.protos.Message;
import com.raven.common.utils.SnowFlake;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientPresureOwner {

    public static SnowFlake snowFlake = new SnowFlake(1, 2);

    public static void main(String[] args) throws Exception {
        String uid = "user1";
        List<String> members = new ArrayList<>();
        members.add(uid);
        for (int i = 2; i <2501; i++) {
            String member = "user" + i;
            members.add(member);
        }
//        GroupOutParam groupInfo = Utils.newGroup(members);
        String token = Utils.getToken(uid);
//        log.info("$$$$$:{}",groupInfo.getGroupId());
        loginAndSendMessage(uid, token, "lBOx-9GTSrEnlpOChMccy0");
    }

    private static void loginAndSendMessage(String uid, String token, String groupId)
        throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
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
                    pipeline.addLast(new ClientPresureOwnerHandler(uid, groupId, token));
                }
            });
//        ServerInfoOutParam outParam = Utils.getAccessAddress(token);
        b.connect("35.229.128.80", 7010);
    }

}

