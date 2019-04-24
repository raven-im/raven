package com.raven.access.handler.server;

import com.raven.common.model.ConverInfo;
import com.raven.common.model.MsgContent;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.HisMessagesAck;
import com.raven.common.protos.Message.HisMessagesReq;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.MessageType;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
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
public class HistoryHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private ConverManager converManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {
        if (message.getType() == Type.HisMessagesReq) {
            HisMessagesReq req = message.getHisMessagesReq();
            log.info("receive history message request:{}", req);
            ConverInfo info = converManager.getConverInfo(req.getConverId());
            if (null != info) {
                List<MsgContent> msgContents = converManager
                    .getHistoryMsg(req.getConverId(), req.getBeaginTime());
                List<MessageContent> contentList = new ArrayList<>();
                for (MsgContent msgContent : msgContents) {
                    MessageContent content = MessageContent.newBuilder().setId(msgContent.getId())
                        .setUid(msgContent.getUid())
                        .setContent(msgContent.getContent()).setTime(msgContent.getTime()).setType(
                            MessageType.valueOf(msgContent.getType())).build();
                    contentList.add(content);
                }
                HisMessagesAck hisMessagesAck = HisMessagesAck.newBuilder().setId(req.getId())
                    .setConverId(req.getConverId()).addAllMessageList(contentList).build();
                RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.HisMessagesAck)
                    .setHisMessagesAck(hisMessagesAck).build();
                ctx.writeAndFlush(ravenMessage);
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }

}
