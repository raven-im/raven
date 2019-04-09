package com.tim.single.tcp.client;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private UpDownMessage message;

    public ClientHandler(UpDownMessage message) {
        this.message = message;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendPrivateMessage(message);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg) {
        if (msg instanceof MessageAck) {
            MessageAck ack = (MessageAck) msg;
            log.info(ack.getConversasionId());
            messageConnectionCtx.close();
//            sendPrivateMessage();
        }
    }

    private void sendPrivateMessage(UpDownMessage message) {
        log.info("send message {}", message);
        messageConnectionCtx.writeAndFlush(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
