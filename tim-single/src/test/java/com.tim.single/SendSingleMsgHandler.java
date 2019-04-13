package com.tim.single;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendSingleMsgHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private UpDownMessage message;

    private MessageListener listener;

    public SendSingleMsgHandler(UpDownMessage message, MessageListener listener) {
        this.message = message;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendSingleMessage(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg) {
        if (msg instanceof MessageAck) {
            MessageAck ack = (MessageAck) msg;
            log.info(ack.getConverId());
            listener.onMessageAckReceived(ack);
        } else if (msg instanceof UpDownMessage) {
            UpDownMessage downMessage = (UpDownMessage) msg;
            log.info(downMessage.getConverId());
        }
    }

    private void sendSingleMessage(UpDownMessage message) {
        ByteBuf byteBuf = Utils.pack2Client(message);
        messageConnectionCtx.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
