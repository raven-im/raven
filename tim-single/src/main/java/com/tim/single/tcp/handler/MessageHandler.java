package com.tim.single.tcp.handler;

import com.google.protobuf.MessageLite;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ServerChannelManager;
import com.tim.common.protos.Auth.ServerInfo;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConverType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.single.tcp.manager.SenderManager;
import com.tim.storage.conver.ConverManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private SenderManager senderManager;

    @Autowired
    private ServerChannelManager channelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageLite messageLite)
        throws Exception {
        if (messageLite instanceof UpDownMessage) {
            UpDownMessage message = (UpDownMessage) messageLite;
            String convId;
            if (message.getConverType() == ConverType.SINGLE) {
                log.info("received msg id:{}", message.getId());
                if (!StringUtils.isEmpty(message.getConverId())) {
                    // validate the conversation id.
                    if (!converManager.isSingleConverIdValid(message.getConverId())) {
                        log.error("illegal conversation id.");
                        sendACK(ctx, message, Code.FAIL);
                        return;
                    } else {
                        convId = message.getConverId();
                    }
                } else {
                    convId = converManager
                        .newSingleConverId(message.getFromUid(), message.getTargetUid());
                    message.toBuilder().setConverId(convId);
                }
                // access server ACK.
                converManager.cacheMsg2Conver(message.getContent(), convId);
                sendACK(ctx, message, Code.SUCCESS);
                UpDownMessage downMessage = UpDownMessage.newBuilder()
                    .setId(message.getId())
                    .setFromUid(message.getFromUid())
                    .setTargetUid(message.getTargetUid())
                    .setConverType(message.getConverType())
                    .setContent(message.getContent())
                    .setConverId(convId)
                    .build();
                senderManager.addMessage(downMessage);
            } else {
                log.error("illegal Message.");
                sendACK(ctx, message, Code.FAIL);
            }
        } else if (messageLite instanceof ServerInfo) {
            ServerInfo serverInfo = (ServerInfo) messageLite;
            Server server = new Server(serverInfo.getIp(), serverInfo.getPort());
            log.info("tim access server connect success ip:{},port{}",server.getIp(),server.getPort());
            channelManager.addServer2Channel(server, ctx.channel());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("client disconnected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
        Server server = channelManager.getServerByChannel(ctx.channel());
        channelManager.removeServer(server);
    }

    private void sendACK(ChannelHandlerContext ctx, UpDownMessage message, Code code) {
        MessageAck messageAck = MessageAck.newBuilder()
            .setId(message.getId())
            .setTargetUid(message.getFromUid())
            .setCid(message.getCid())
            .setCode(code)
            .setTime(System.currentTimeMillis())
            .setConverId(message.getConverId())
            .build();
        ctx.writeAndFlush(messageAck);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

}
