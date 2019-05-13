package com.raven.access.server;

import static io.netty.buffer.Unpooled.wrappedBuffer;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.raven.access.handler.server.ConversationHandler;
import com.raven.access.handler.server.HeartBeatHandler;
import com.raven.access.handler.server.HistoryHandler;
import com.raven.access.handler.server.LoginAuthHandler;
import com.raven.access.handler.server.MesaageHandler;
import com.raven.common.protos.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccessWebsocketServer {

    @Value("${netty.websocket.port}")
    private int nettyWebsocketPort;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    private EventLoopGroup workGroup = new NioEventLoopGroup();

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    @Autowired
    private LoginAuthHandler loginAuthHandler;

    @Autowired
    private MesaageHandler mesaageHandler;

    @Autowired
    private ConversationHandler conversationHandler;

    @Autowired
    private HistoryHandler historyHandler;

    @PostConstruct
    public void startServer() {
        startMessageServer();
    }

    private void startMessageServer() {
        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup, workGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel)
                    throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new IdleStateHandler(10, 10, 15));
                    // HTTP请求的解码和编码
                    pipeline.addLast(new HttpServerCodec());
                    // 把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse，
                    // 原因是HTTP解码器会在每个HTTP消息中生成多个消息对象HttpRequest/HttpResponse,HttpContent,LastHttpContent
                    pipeline.addLast(new HttpObjectAggregator(65536));
                    // 主要用于处理大数据流，比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的; 增加之后就不用考虑这个问题了
                    pipeline.addLast(new ChunkedWriteHandler());
                    // WebSocket数据压缩
                    pipeline.addLast(new WebSocketServerCompressionHandler());
                    // 协议包长度限制
                    pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 1024 * 10));
                    // 协议包解码
                    pipeline.addLast(new MessageToMessageDecoder<WebSocketFrame>() {
                        @Override
                        protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
                            if (frame instanceof BinaryWebSocketFrame) {
                                ByteBuf buf = frame.content();
                                out.add(buf);
                                buf.retain();
                            } else if (frame instanceof PingWebSocketFrame) {
                                ctx.channel()
                                    .writeAndFlush(new PongWebSocketFrame(frame.content()
                                        .retain()));
                            } else {
                                throw new IllegalStateException("Unsupported web socket msg " + frame);
                            }
                        }
                    });
                    // 协议包编码
                    pipeline.addLast(new MessageToMessageEncoder<MessageLiteOrBuilder>() {
                        @Override
                        protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
                            ByteBuf result = null;
                            if (msg instanceof MessageLite) {
                                result = wrappedBuffer(((MessageLite) msg).toByteArray());
                            }
                            if (msg instanceof MessageLite.Builder) {
                                result = wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
                            }
                            // ==== 上面代码片段是拷贝自TCP ProtobufEncoder 源码 ====
                            // 然后下面再转成websocket二进制流，因为客户端不能直接解析protobuf编码生成的
                            WebSocketFrame frame = new BinaryWebSocketFrame(result);
                            out.add(frame);
                        }
                    });
                    // 协议包解码时指定Protobuf字节数实例化为RavenMessage类型
                    pipeline.addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()));
                    // 业务处理器
                    pipeline.addLast("LoginAuthHandler", loginAuthHandler);
                    pipeline.addLast("HeartBeatHandler", heartBeatHandler);
                    pipeline.addLast("MesaageHandler", mesaageHandler);
                    pipeline.addLast("ConversationHandler", conversationHandler);
                    pipeline.addLast("HistoryHandler", historyHandler);

                }
            });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(nettyWebsocketPort)).addListener(future -> {
            if (future.isSuccess()) {
                log.info("raven-access websocket server start success on port:{}", nettyWebsocketPort);
            } else {
                log.error("raven-access websocket server start failed!");
            }
        });
    }

    private void bindConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //调试用
    }

    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workGroup.shutdownGracefully().syncUninterruptibly();
        log.info("close raven-access websocket server success");
    }

}
