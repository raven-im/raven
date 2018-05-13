package auth;

import auth.handler.AuthMessageConnectionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.code.MessageDecoder;
import protobuf.code.MessageEncoder;

/**
 * Created by win7 on 2016/3/5.
 */
public class AuthMessageConnection {

    private static final Logger logger = LoggerFactory.getLogger(AuthMessageConnection.class);

    public static void startAuthMessageConnection(String ip, int port) {
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
                        pipeline.addLast("AuthMessageConnectionHandler",
                                new AuthMessageConnectionHandler());  //Auth -> gate
                    }
                });
        bootstrap.connect(ip, port).addListener(future -> {
            if (future.isSuccess()) {
                logger.info(
                        "auth connect message success");
            } else {
                logger.error("auth connect message failed!");
            }
        });
    }
}
