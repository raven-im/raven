package com.tim.single;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Common;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Common.MessageType;
import com.tim.common.protos.Message.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<MessageLite> {

    private ChannelHandlerContext messageConnectionCtx;

    private String fromUserId = "user1";
    private String targetUserId = "user2";
    private String convId = "user1_user2";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendPrivateMessage();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite msg) {
        if (msg instanceof MessageAck) {
            MessageAck ack = (MessageAck) msg;
            log.info(ack.getConversasionId());
//            sendPrivateMessage();
        }
    }

    private void sendPrivateMessage() {
        Common.MessageContent content = Common.MessageContent.newBuilder()
            .setId(1)
            .setUid(fromUserId)
            .setTime(System.currentTimeMillis())
            .setType(MessageType.TEXT)
            .setContent("Hello world!")
            .build();

        UpDownMessage msg = UpDownMessage.newBuilder()
            .setId(100)
//            .setClientId(1000)
            .setFromId(fromUserId)
            .setTargetId(targetUserId)
            .setConversationType(ConversationType.SINGLE)
//            .setConversasionId(convId)
            .setContent(content)
            .setDirection(Direction.SS)
            .build();
        ByteBuf byteBuf = Utils.pack2Client(msg);
        messageConnectionCtx.writeAndFlush(byteBuf);
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
