package gate;

import gate.handler.GateMessageConnectionHandler;
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
 * Created by Dell on 2016/2/2.
 */
public class GateMessageConnection {

    private static final Logger logger = LoggerFactory.getLogger(GateMessageConnection.class);

    public static void startGateMessageConnection(String ip, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel)
                            throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("MessageDecoder", new MessageDecoder());
                        pipeline.addLast("MessageEncoder", new MessageEncoder());
                        pipeline.addLast("GateMessageConnectionHandler",
                                new GateMessageConnectionHandler());  //meaage -> gate
                    }
                });
        bootstrap.connect(ip, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    logger.info("gate connect message sucess ");
                } else {
                    logger.error("gate connect message failed! ");
                }
            }
        });

    }
}
