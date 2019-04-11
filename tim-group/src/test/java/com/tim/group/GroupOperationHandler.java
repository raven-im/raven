package com.tim.group;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Group.GroupAck;
import com.tim.common.protos.Group.GroupCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupOperationHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private GroupCmd command;

    private GroupListener listener;

    public GroupOperationHandler(GroupCmd cmd, GroupListener listener) {
        this.command = cmd;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendGroupCmd(command);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg) {
        if (msg instanceof GroupAck) {
            GroupAck ack = (GroupAck) msg;
            listener.onGroupAckReceived(ack);
        }
    }

    private void sendGroupCmd(GroupCmd cmd) {
        ByteBuf byteBuf = Utils.pack2Client(cmd);
        messageConnectionCtx.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
