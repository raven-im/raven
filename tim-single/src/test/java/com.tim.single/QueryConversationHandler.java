package com.tim.single;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Conversation.ConversationAck;
import com.tim.common.protos.Conversation.ConversationReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryConversationHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private ConversationReq request;
    private MessageListener listener;

    public QueryConversationHandler(ConversationReq req, MessageListener listener) {
        this.request = req;
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendQueryConversation(request);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg) {
        if (msg instanceof ConversationAck) {
            ConversationAck ack = (ConversationAck) msg;
            listener.onQueryAck(ack);
        }
    }

    private void sendQueryConversation(ConversationReq req) {
        ByteBuf byteBuf = Utils.pack2Client(req);
        messageConnectionCtx.writeAndFlush(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
