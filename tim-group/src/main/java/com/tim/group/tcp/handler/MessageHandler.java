package com.tim.group.tcp.handler;

import com.google.protobuf.MessageLite;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ServerChannelManager;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Message.Direction;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;

import com.tim.group.tcp.manager.ConversationManager;
import com.tim.group.tcp.manager.SenderManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private ConversationManager conversationManager;

    @Autowired
    private SenderManager senderManager;

    @Autowired
    private ServerChannelManager channelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite) throws Exception {
//        if (messageLite instanceof UpDownMessage) {
//            UpDownMessage message = (UpDownMessage) messageLite;
//            String convId;
//            if (message.getConversationType() == ConversationType.SINGLE ) {
//                log.info("received msg id:{}", message.getId());
//                if (!StringUtils.isEmpty(message.getConversasionId())) {
//                    // validate the conversation id.
//                    if (!conversationManager.isSingleConvIdValid(message.getConversasionId(),
//                        message.getFromId(), message.getTargetId())) {
//                        log.error("illegal conversation id.");
//                        sendACK(ctx, message.getId(), message.getFromId(), Code.FAIL, message.getConversasionId());
//                        return;
//                    } else {
//                        convId = message.getConversasionId();
//                    }
//                } else {
//                    convId = conversationManager.newConversationId(message.getFromId(), message.getTargetId());
//                }
//                // access server ACK.
//                conversationManager.cacheConversation(message, convId);
//                sendACK(ctx, message.getId(), message.getFromId(), Code.SUCCESS, convId);
//
//                UpDownMessage downMessage = UpDownMessage.newBuilder()
//                    .setId(message.getId())
//                    .setFromId(message.getFromId())
//                    .setTargetId(message.getTargetId())
//                    .setConversationType(message.getConversationType())
//                    .setDirection(Direction.SC)
//                    .setContent(message.getContent())
//                    .setConversasionId(convId)
//                    .build();
//                senderManager.addMessage(downMessage);
//            } else {
//                log.error("illegal Message.");
//                sendACK(ctx, message.getId(), message.getFromId(), Code.FAIL, "");
//            }
//        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
        String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
        Server server = new Server(host, port);
        channelManager.addServer2Channel(server, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("client disconnected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
        String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
        Server server = new Server(host, port);
        channelManager.removeServer(server);
    }

    private void sendACK(ChannelHandlerContext ctx, long id, String targetId, Code code, String convId) {
        MessageAck messageAck = MessageAck.newBuilder()
            .setId(id)
            .setTargetId(targetId)
            .setCode(code)
            .setTime(System.currentTimeMillis())
            .setConversasionId(convId)
            .build();
        ctx.writeAndFlush(messageAck);
    }

}
