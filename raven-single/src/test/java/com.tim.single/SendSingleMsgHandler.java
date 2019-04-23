package com.raven.single;

import com.raven.common.protos.Message.MessageAck;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.single.MessageListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendSingleMsgHandler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private RavenMessage message;

    private MessageListener listener;

    public SendSingleMsgHandler(RavenMessage message, com.raven.single.MessageListener listener) {
        this.message = message;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendSingleMessage(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RavenMessage msg) {
        if (msg.getType() == Type.MessageAck) {
            MessageAck ack = msg.getMessageAck();
            log.info("receive message ack:{}", ack);
            listener.onMessageAckReceived(ack);
        }
    }

    private void sendSingleMessage(RavenMessage message) {
        log.info("send single message");
        messageConnectionCtx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
