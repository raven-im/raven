package com.tim.access.client;


import com.tim.access.config.S2sChannelManager;
import com.tim.access.handler.client.S2sClientHandler;
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
import io.netty.channel.socket.nio.NioSocketChannel;
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
        startConnection();
        startZkWatcher();
    }

    @PreDestroy
    public void destroy() {
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close tim access client success");
    }

    private void startConnection() {
        for (Server server : s2sChannelManager.getSingleServerList()) {
            connectServer(server);
        }
    }

    private void connectServer(Server server) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast("MessageDecoder", new MessageDecoder());
                    p.addLast("MessageEncoder", new MessageEncoder());
                    p.addLast("S2sClientHandler", s2sClientHandler);
                }
            });
        ChannelFuture future = bootstrap.connect(server.getIp(), server.getPort())
            .syncUninterruptibly();
        if (future.isSuccess()) {
            s2sChannelManager.addServer2Channel(server, future.channel());
            log.info("connect server success ip:{},port:{}", server.getIp(),
                server.getPort());
        } else {
            log.error("connect server failed ip:{},port:{}", server.getIp(),
                server.getPort());
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
                    int port = Integer.valueOf(metadata.get("netty-port").toString());
                    Server server = new Server(address, port);
                    connectServer(server);
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
                    int port = Integer.valueOf(metadata.get("netty-port").toString());
                    Server server = new Server(address, port);
                    connectServer(server);
                }
            }
        });
        groupWatcher.start();
        log.info("start group server zk watcher");
    }


}
