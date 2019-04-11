package com.tim.group;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupMsgHandler extends SimpleChannelInboundHandler<MessageLite> {
    private ChannelHandlerContext messageConnectionCtx;

    private UpDownMessage message;

    private GroupListener listener;

    public GroupMsgHandler(UpDownMessage msg, GroupListener listener) {
        this.message = msg;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendGroupMsg(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg) {
        if (msg instanceof MessageAck) {
            MessageAck ack = (MessageAck) msg;
            listener.onMessageAckReceived(ack);
        }
    }

    private void sendGroupMsg(UpDownMessage cmd) {
        ByteBuf byteBuf = Utils.pack2Client(cmd);
        messageConnectionCtx.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
