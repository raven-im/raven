package com.tim.single;

import com.tim.common.protos.Message.*;
import com.tim.common.protos.Message.TimMessage.Type;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendSingleMsgHandler extends SimpleChannelInboundHandler<TimMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private TimMessage message;

    private MessageListener listener;

    public SendSingleMsgHandler(TimMessage message, MessageListener listener) {
        this.message = message;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendSingleMessage(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TimMessage msg) {
        if (msg.getType() == Type.MessageAck) {
            MessageAck ack = msg.getMessageAck();
            log.info("receive message ack:{}", ack);
            listener.onMessageAckReceived(ack);
        }
    }

    private void sendSingleMessage(TimMessage message) {
        log.info("send single message");
        messageConnectionCtx.writeAndFlush(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
