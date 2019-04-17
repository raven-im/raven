package com.tim.access.client;


import com.tim.access.config.S2sChannelManager;
import com.tim.access.handler.client.S2sClientHandler;
import com.tim.common.loadbalance.Server;
import com.tim.common.protos.Message;
import com.tim.common.utils.Constants;
import com.tim.common.utils.JsonHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@DependsOn("s2sChannelManager")
public class AccessTcpClient {

    @Autowired
    private S2sChannelManager s2sChannelManager;

    @Autowired
    private S2sClientHandler s2sClientHandler;

    @Autowired
    private CuratorFramework curator;

    @Value("${discovery.single-server-path}")
    private String singleServerPath;

    @Value("${discovery.group-server-path}")
    private String groupServerPath;

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    @PostConstruct
    public void startClient() throws Exception {
        startZkWatcher();
    }

    @PreDestroy
    public void destroy() {
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close tim-access client success");
    }

    private Channel connectServer(Server server) {
        if (null != s2sChannelManager.getChannelByServer(server)) {
            return null;
        }
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new IdleStateHandler(10, 10, 15));
                    p.addLast(new ProtobufVarint32FrameDecoder());
                    p.addLast(new ProtobufDecoder(Message.TimMessage.getDefaultInstance()));
                    // 对protobuf协议的消息头上加上一个长度为32的整形字段
                    p.addLast(new ProtobufVarint32LengthFieldPrepender());
                    p.addLast(new ProtobufEncoder());
                    p.addLast("S2sClientHandler", s2sClientHandler);
                }
            });
        ChannelFuture future = bootstrap.connect(server.getIp(), server.getPort())
            .syncUninterruptibly();
        if (future.isSuccess()) {
            log.info("connect server success ip:{},port:{}", server.getIp(),
                server.getPort());
            return future.channel();
        } else {
            log.error("connect server failed ip:{},port:{}", server.getIp(),
                server.getPort());
        }
        return null;
    }

    private void startZkWatcher() throws Exception {
        PathChildrenCache singleWatcher = new PathChildrenCache(curator, singleServerPath, true);
        singleWatcher.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curator,
                PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(Type.CHILD_ADDED)) {
                    log.info("zookeeper watched add single server node:{}",
                        new String(event.getData().getData()));
                    Map<String, Object> data = JsonHelper
                        .strToMap(new String(event.getData().getData()));
                    String address = (String) data.get("address");
                    Map<String, Object> payload = (Map<String, Object>) data.get("payload");
                    Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");
                    int port = Integer.valueOf(metadata.get(Constants.CONFIG_TCP_PORT).toString());
                    Server server = new Server(address, port);
                    Channel channel = connectServer(server);
                    if (null != channel) {
                        s2sChannelManager.addSingleServer(server, channel);
                    }
                }
            }
        });
        singleWatcher.start();
        log.info("start single server zk watcher");
        PathChildrenCache groupWatcher = new PathChildrenCache(curator, groupServerPath, true);
        groupWatcher.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curator,
                PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(Type.CHILD_ADDED)) {
                    log.info("zookeeper watched add group server node:{}",
                        new String(event.getData().getData()));
                    Map<String, Object> data = JsonHelper
                        .strToMap(new String(event.getData().getData()));
                    String address = (String) data.get("address");
                    Map<String, Object> payload = (Map<String, Object>) data.get("payload");
                    Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");
                    int port = Integer.valueOf(metadata.get(Constants.CONFIG_TCP_PORT).toString());
                    Server server = new Server(address, port);
                    Channel channel = connectServer(server);
                    if (null != channel) {
                        s2sChannelManager.addGroupServer(server, channel);
                    }
                }
            }
        });
        groupWatcher.start();
        log.info("start group server zk watcher");
    }


}
