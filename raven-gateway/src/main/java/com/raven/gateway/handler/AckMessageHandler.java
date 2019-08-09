package com.raven.gateway.handler;


import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.MessageAck;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.storage.conver.ConverManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class AckMessageHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private ConverManager converManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage msg) throws Exception {
        if (msg.getType() == Type.MessageAck) {
            MessageAck messageAck = msg.getMessageAck();
            log.info("receive ack message:{}", JsonHelper.toJsonString(messageAck));
            String uid = uidChannelManager.getIdByChannel(ctx.channel());
            converManager.delWaitUserAckMsg(uid, messageAck.getConverId(), messageAck.getId());
            converManager.updateUserReadMessageId(uid,messageAck.getConverId(), messageAck.getId());
        } else {
            ctx.fireChannelRead(msg);
        }

    }
}
