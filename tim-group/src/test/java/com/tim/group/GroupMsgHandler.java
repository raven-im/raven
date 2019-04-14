package com.tim.group;

import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupMsgHandler extends SimpleChannelInboundHandler<TimMessage> {
    private ChannelHandlerContext messageConnectionCtx;

    private TimMessage message;

    private GroupListener listener;

    public GroupMsgHandler(TimMessage msg, GroupListener listener) {
        this.message = msg;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendGroupMsg(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TimMessage msg) {
        if (msg.getType() == Type.MessageAck) {
            MessageAck ack = msg.getMessageAck();
            listener.onMessageAckReceived(ack);
        }
    }

    private void sendGroupMsg(TimMessage msg) {
        messageConnectionCtx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
