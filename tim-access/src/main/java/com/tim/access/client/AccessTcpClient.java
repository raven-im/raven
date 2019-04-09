package com.tim.access.client;


import com.tim.access.config.S2sChannelManager;
import com.tim.common.code.MessageDecoder;
import com.tim.common.code.MessageEncoder;
import com.tim.common.loadbalance.Server;
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
import io.netty.channel.socket.nio.NioServerSocketChannel;
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

    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    @Autowired
    private S2sChannelManager s2sChannelManager;

    @Autowired
    private CuratorFramework curator;

    @Value("${discovery.single-server-path}")
    private String singleServerPath;

    @Value("${discovery.group-server-path}")
    private String groupServerPath;

    private Bootstrap bootstrap = new Bootstrap();

    @PostConstruct
    public void startClient() throws Exception {
        startTcpClient();
    }

    private void startTcpClient() throws Exception {

        bootstrap.group(new NioEventLoopGroup())
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new IdleStateHandler(10, 10, 15));
                    // 处理半包消息的解码类
                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                    pipeline.addLast(new MessageDecoder());
                    // 对protobuf协议的消息头上加上一个长度为32的整形字段
                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    pipeline.addLast(new MessageEncoder());
                }
            });
        startConnection();
        startZkWatcher();
    }


    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close tim access client success");
    }

    private void startConnection() {
        s2sChannelManager.getSingleServerList().forEach(server -> {
            connectServer(server);
        });
        s2sChannelManager.getGroupServerList().forEach(server -> {
            connectServer(server);
        });
    }

    private void connectServer(Server server) {
        ChannelFuture future = bootstrap.connect(server.getIp(), server.getPort());
        if (future.isSuccess()) {
            s2sChannelManager.addServer2Channel(server, future.channel());
            log.info("connect server success ip:{},port:{}", server.getIp(),
                server.getPort());
        } else {
            log.error("connect server failed ip:{},port:{}", server.getIp(),
                server.getPort());
        }
    }

    private void disConnectServer(Server server) {
        Channel channel = s2sChannelManager.getChannelByServer(server);
        s2sChannelManager.removeServer(server);
        int i = 0;
        while (i < 5) {
            i++;
            ChannelFuture future = channel.closeFuture();
            if (future.isSuccess()) {
                log.info("disconnect server success ip:{},port:{}", server.getIp(),
                    server.getPort());
                break;
            }
        }
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
                    int port = (int) metadata.get("netty-port");
                    Server server = new Server(address, port);
                    connectServer(server);
                }
                if (event.getType().equals(Type.CHILD_REMOVED)) {
                    log.info("zookeeper watched remove single server node:{}",
                        new String(event.getData().getData()));
                    Map<String, Object> data = JsonHelper
                        .strToMap(new String(event.getData().getData()));
                    String address = (String) data.get("address");
                    Map<String, Object> payload = (Map<String, Object>) data.get("payload");
                    Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");
                    int port = (int) metadata.get("netty-port");
                    Server server = new Server(address, port);
                    disConnectServer(server);
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
                    int port = (int) metadata.get("netty-port");
                    Server server = new Server(address, port);
                    connectServer(server);
                }
                if (event.getType().equals(Type.CHILD_REMOVED)) {
                    log.info("zookeeper watched remove group server node:{}",
                        new String(event.getData().getData()));
                    Map<String, Object> data = JsonHelper
                        .strToMap(new String(event.getData().getData()));
                    String address = (String) data.get("address");
                    Map<String, Object> payload = (Map<String, Object>) data.get("payload");
                    Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");
                    int port = (int) metadata.get("netty-port");
                    Server server = new Server(address, port);
                    disConnectServer(server);
                }
            }
        });
        groupWatcher.start();
        log.info("start group server zk watcher");
    }


}
