package com.tim.access.client;


import com.tim.common.code.MessageDecoder;
import com.tim.common.code.MessageEncoder;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ClientChannelManager;
import io.netty.bootstrap.Bootstrap;
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
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccessTcpClient {

    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    @Resource(name = "singleServerList")
    private List<Server> singleServerList;

    @Resource(name = "groupServerList")
    private List<Server> groupServerList;

    private ClientChannelManager ssChannelManager;

    @PostConstruct
    public void startClient() {
        startTcpClient();
    }

    private void startTcpClient() {
        Bootstrap bootstrap = new Bootstrap();
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
        startConnection(bootstrap);
    }


    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close tim access client success");
    }

    private void startConnection(Bootstrap bootstrapb) {
        // TODO 增加监听节点变化
        singleServerList.forEach(server -> {
            ChannelFuture future = bootstrapb.connect(server.getIp(), server.getPort());
            if (future.isSuccess()) {
                ssChannelManager.addServer2Channel(server, future.channel());
                log.info("connect singleServer success ip:{},port:{}", server.getIp(),
                    server.getPort());
            } else {
                log.error("connect singleServer failed ip:{},port:{}", server.getIp(),
                    server.getPort());
            }
        });
        groupServerList.forEach(server -> {
            ChannelFuture future = bootstrapb.connect(server.getIp(), server.getPort());
            if (future.isSuccess()) {
                ssChannelManager.addServer2Channel(server, future.channel());
                log.info("connect groupServer success ip:{},port:{}", server.getIp(),
                    server.getPort());
            } else {
                log.error("connect groupServer failed ip:{},port:{}", server.getIp(),
                    server.getPort());
            }
        });
    }

}
