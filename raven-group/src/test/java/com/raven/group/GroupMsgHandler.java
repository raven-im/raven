package com.raven.group;

import com.raven.common.protos.Message.MessageAck;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupMsgHandler extends SimpleChannelInboundHandler<RavenMessage> {
    private ChannelHandlerContext messageConnectionCtx;

    private RavenMessage message;

    private GroupListener listener;

    public GroupMsgHandler(RavenMessage msg, GroupListener listener) {
        this.message = msg;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendGroupMsg(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RavenMessage msg) {
        if (msg.getType() == Type.MessageAck) {
            MessageAck ack = msg.getMessageAck();
            listener.onMessageAckReceived(ack);
        }
    }

    private void sendGroupMsg(RavenMessage msg) {
        messageConnectionCtx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
