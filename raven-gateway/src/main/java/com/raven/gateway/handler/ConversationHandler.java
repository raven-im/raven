package com.raven.gateway.handler;

import com.raven.common.model.MsgContent;
import com.raven.common.model.UserConversation;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.ConverAck;
import com.raven.common.protos.Message.ConverInfo;
import com.raven.common.protos.Message.ConverInfo.Builder;
import com.raven.common.protos.Message.ConverReq;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.MessageType;
import com.raven.common.protos.Message.OperationType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.storage.conver.ConverManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class ConversationHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private ConverManager converManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {
        if (message.getType() == Type.ConverReq) {
            ConverReq conversationReq = message.getConverReq();
            processConverReqMsg(ctx, conversationReq);
        } else {
            ctx.fireChannelRead(message);
        }
    }

    private void sendFailAck(ChannelHandlerContext ctx, Long id, Code code) {
        ConverAck converAck = ConverAck.newBuilder()
            .setId(id)
            .setCode(code)
            .setTime(System.currentTimeMillis())
            .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder()
            .setType(Type.ConverAck)
            .setConverAck(converAck).build();
        ctx.writeAndFlush(ravenMessage);
    }

    private void processConverReqMsg(ChannelHandlerContext ctx, ConverReq conversationReq) {
        log.debug("receive conver request message:{}", JsonHelper.toJsonString(conversationReq));
        if (conversationReq.getType() == OperationType.DETAIL) {
            UserConversation userConversation = converManager
                .getConverListInfo(uidChannelManager.getIdByChannel(ctx.channel()),
                    conversationReq.getConversationId());
            if (null == userConversation) {
                sendFailAck(ctx, conversationReq.getId(), Code.OPERATION_TYPE_INVALID);
            }
            ConverInfo info = buildConverInfo(userConversation);
            ConverAck converAck = ConverAck.newBuilder()
                .setId(conversationReq.getId())
                .setCode(Code.SUCCESS)
                .setTime(System.currentTimeMillis())
                .setConverInfo(info)
                .build();
            RavenMessage ravenMessage = RavenMessage.newBuilder()
                .setType(Type.ConverAck)
                .setConverAck(converAck).build();
            ctx.writeAndFlush(ravenMessage);
        } else if (conversationReq.getType() == OperationType.ALL) {
            List<UserConversation> converList = converManager
                .getConverListByUid(uidChannelManager.getIdByChannel(ctx.channel()));
            List<ConverInfo> converInfos = new ArrayList<>();
            for (UserConversation converListInfo : converList) {
                ConverInfo info = buildConverInfo(converListInfo);
                converInfos.add(info);
            }
            ConverAck converAck = ConverAck.newBuilder()
                .setId(conversationReq.getId())
                .setCode(Code.SUCCESS)
                .setTime(System.currentTimeMillis())
                .addAllConverList(converInfos)
                .build();
            RavenMessage ravenMessage = RavenMessage.newBuilder()
                .setType(Type.ConverAck)
                .setConverAck(converAck).build();
            ctx.writeAndFlush(ravenMessage);
        } else {
            sendFailAck(ctx, conversationReq.getId(), Code.OPERATION_TYPE_INVALID);
        }
    }

    private ConverInfo buildConverInfo(UserConversation userConversation) {
        Builder builder = ConverInfo.newBuilder();
        builder.setConverId(userConversation.getId());
        builder.setType(ConverType.valueOf(userConversation.getType()));
        builder.addAllUidList(userConversation.getUidList());
        builder.setTime(userConversation.getTime());
        if (ConverType.valueOf(userConversation.getType()) == ConverType.GROUP) {
            builder.setGroupId(userConversation.getGroupId());
        }
        MsgContent msgContent = userConversation.getLastContent();
        if (msgContent != null) {
            MessageContent content = MessageContent.newBuilder().setId(msgContent.getId())
                .setUid(msgContent.getUid())
                .setType(MessageType.valueOf(msgContent.getType()))
                .setContent(msgContent.getContent())
                .setTime(msgContent.getTime())
                .build();
            builder.setLastContent(content);
        }
        builder.setReadMsgId(userConversation.getReadMsgId());
        ConverInfo info = builder.build();
        return info;
    }
}