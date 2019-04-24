package com.raven.client.group;

import com.fasterxml.jackson.databind.JsonNode;
import com.raven.client.group.bean.GroupOutParam;
import com.raven.client.group.bean.GroupReqParam;
import com.raven.common.protos.Message;
import com.raven.common.utils.JsonHelper;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Author zxx Description Simple client for module test Date Created on 2018/5/25
 */
public class ClientOwner {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 7010;
    public static SnowFlake snowFlake = new SnowFlake(1, 2);

    public static void main(String[] args) throws Exception {
        GroupOutParam groupInfo = newGroup();
        loginAndSendMessage(groupInfo.getGroupId());
    }

    private static void loginAndSendMessage(String groupId) throws InterruptedException {
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
                    pipeline.addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()));
                    // 对protobuf协议的消息头上加上一个长度为32的整形字段
                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    pipeline.addLast(new ProtobufEncoder());
                    pipeline.addLast(new ClientOwnerHandler( "owner", groupId));
                }
            });
        b.connect(HOST, PORT);
    }

    private static GroupOutParam newGroup() {
        String groupName = "test_group";
        String portrait = "http://google.com/1.jpg";
        List<String> members = Arrays.asList("owner", "invitee1", "invitee2");
        GroupReqParam param = new GroupReqParam(groupName, portrait, members);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost("http://localhost:8370/group/create");
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        try {
            StringEntity stringEntity = new StringEntity(JsonHelper.toJsonString(param), "UTF-8");
            stringEntity.setContentEncoding("UTF-8");

            httpPost.setEntity(stringEntity);
            // Create a custom response handler
            ResponseHandler<String> responseHandler = (response) -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();

                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody = httpclient.execute(httpPost, responseHandler);
            JsonNode node = JsonHelper.getJacksonMapper().readTree(responseBody);

            JsonNode nodeGroup = JsonHelper.getJacksonMapper().readTree(node.get("data").toString());
            return new GroupOutParam(nodeGroup.get("groupId").asText(), nodeGroup.get("converId").asText());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

