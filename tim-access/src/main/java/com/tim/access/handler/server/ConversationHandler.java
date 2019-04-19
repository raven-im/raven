package com.tim.access.handler.server;

import com.tim.common.model.ConverInfo;
import com.tim.common.model.ConverListInfo;
import com.tim.common.model.MsgContent;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.protos.Message;
import com.tim.common.protos.Message.Code;
import com.tim.common.protos.Message.ConverAck;
import com.tim.common.protos.Message.ConverInfo.Builder;
import com.tim.common.protos.Message.ConverReq;
import com.tim.common.protos.Message.ConverType;
import com.tim.common.protos.Message.MessageContent;
import com.tim.common.protos.Message.MessageType;
import com.tim.common.protos.Message.OperationType;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
import com.tim.storage.conver.ConverManager;
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
public class ConversationHandler extends SimpleChannelInboundHandler<TimMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private ConverManager converManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
        TimMessage message) throws Exception {
        if (message.getType() == Type.ConverReq) {
            ConverReq conversationReq = message.getConverReq();
            log.info("receive conver request message:{}", conversationReq);
            if (conversationReq.getType() == OperationType.DETAIL) {
                ConverInfo converInfo = converManager
                    .getConverInfo(conversationReq.getConversationId());
                if (null == converInfo) {
                    sendFailAck(ctx, conversationReq.getId());
                }
                Builder builder = Message.ConverInfo.newBuilder();
                builder.setConverId(converInfo.getId());
                builder.setType(ConverType.valueOf(converInfo.getType()));
                if (ConverType.valueOf(converInfo.getType()) == ConverType.SINGLE) {
                    builder.addAllUidList(converInfo.getUidList());
                }
                if (ConverType.valueOf(converInfo.getType()) == ConverType.GROUP) {
                    builder.setGroupId(converInfo.getGroupId());
                }
                Message.ConverInfo info = builder.build();
                ConverAck converAck = ConverAck.newBuilder()
                    .setId(conversationReq.getId())
                    .setCode(Code.SUCCESS)
                    .setTime(System.currentTimeMillis())
                    .setConverInfo(info)
                    .build();
                TimMessage timMessage = TimMessage.newBuilder()
                    .setType(Type.ConverAck)
                    .setConverAck(converAck).build();
                ctx.writeAndFlush(timMessage);
            } else if (conversationReq.getType() == OperationType.ALL) {
                List<ConverListInfo> converList = converManager
                    .getConverListByUid(uidChannelManager.getIdByChannel(ctx.channel()));
                List<Message.ConverInfo> converInfos = new ArrayList<>();
                for (ConverListInfo converListInfo : converList) {
                    MsgContent msgContent = converListInfo.getLastContent();
                    MessageContent content = MessageContent.newBuilder().setId(msgContent.getId())
                        .setUid(msgContent.getUid())
                        .setType(MessageType.valueOf(msgContent.getType()))
                        .setContent(msgContent.getContent())
                        .setTime(msgContent.getTime())
                        .build();

                    Builder builder = Message.ConverInfo.newBuilder();
                    builder.setConverId(converListInfo.getId());
                    builder.setType(ConverType.valueOf(converListInfo.getType()));
                    if (ConverType.valueOf(converListInfo.getType()) == ConverType.SINGLE) {
                        builder.addAllUidList(converListInfo.getUidList());
                    }
                    if (ConverType.valueOf(converListInfo.getType()) == ConverType.GROUP) {
                        builder.setGroupId(converListInfo.getGroupId());
                    }
                    builder.setLastContent(content);
                    builder.setUnCount(converListInfo.getUnCount());
                    Message.ConverInfo info = builder.build();
                    converInfos.add(info);
                }
                ConverAck converAck = ConverAck.newBuilder()
                    .setId(conversationReq.getId())
                    .setCode(Code.SUCCESS)
                    .setTime(System.currentTimeMillis())
                    .addAllConverList(converInfos)
                    .build();
                TimMessage timMessage = TimMessage.newBuilder()
                    .setType(Type.ConverAck)
                    .setConverAck(converAck).build();
                ctx.writeAndFlush(timMessage);
            } else {
                sendFailAck(ctx, conversationReq.getId());
            }
        }
    }

    private void sendFailAck(ChannelHandlerContext ctx, Long id) {
        ConverAck converAck = ConverAck.newBuilder()
            .setId(id)
            .setCode(Code.FAIL)
            .setTime(System.currentTimeMillis())
            .build();
        TimMessage timMessage = TimMessage.newBuilder()
            .setType(Type.ConverAck)
            .setConverAck(converAck).build();
        ctx.writeAndFlush(timMessage);
    }

}