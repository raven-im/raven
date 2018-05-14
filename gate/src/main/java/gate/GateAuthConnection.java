package gate;

import gate.handler.GateAuthConnectionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.code.MessageDecoder;
import protobuf.code.MessageEncoder;

/**
 * Created by Qzy on 2016/1/28.
 */
public class GateAuthConnection {

    private static final Logger logger = LoggerFactory.getLogger(GateAuthConnection.class);

    public static void startGateAuthConnection(String ip, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("MessageDecoder", new MessageDecoder());
                        pipeline.addLast("MessageEncoder", new MessageEncoder());
                        pipeline.addLast("GateAuthConnectionHandler",
                                new GateAuthConnectionHandler());
                    }
                });
        bootstrap.connect(ip, port).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("gate connect auth sucess ");
            } else {
                logger.error("gate connect auth failed! ");
            }
        });
    }
}
