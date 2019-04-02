package com.tim.single.tcp.process;

import com.tim.common.enums.AckMessageStatus;
import com.tim.common.protos.Message.DownSingle;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpSingle;
import com.tim.single.tcp.channel.NettyChannelManager;
import com.tim.single.tcp.common.BaseMessageProcessor;
import com.tim.single.tcp.common.OfflineMsgService;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PrivateMessageProcessor implements BaseMessageProcessor {

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        UpSingle upMessage = (UpSingle) messageLite;
        sendAck(context, upMessage);
        DownSingle dowmMessage = buildMessage(context, upMessage);
        String uid = upMessage.getToUid();
        List<Channel> channels = nettyChannelManager.getChannelByUid(uid);
        if (channels.isEmpty()) {
            offLineMsgService.storeOfflineMsg(uid, dowmMessage, dowmMessage.getId(),
                dowmMessage.getTimestamp());
        } else {
            offLineMsgService.storeWaitAckMessage(uid, dowmMessage, dowmMessage.getId(),
                dowmMessage.getTimestamp());
            channels.forEach(channel ->
                channel.writeAndFlush(dowmMessage).addListener(future -> {
                    if (!future.isSuccess()) {
                        offLineMsgService
                            .storeOfflineMsg(uid, dowmMessage, dowmMessage.getId(),
                                dowmMessage.getTimestamp());
                    }
                }));
        }
    }

    private DownSingle buildMessage(ChannelHandlerContext context,
        UpSingle upMessage) {
        String fromUid = nettyChannelManager.getUidByChannel(context.channel());
        DownSingle dowmMessage = DownSingle.newBuilder()
            .setFromUid(fromUid)
            .setContent(upMessage.getContent())
            .setId(upMessage.getId())
            .setTimestamp(upMessage.getTimestamp())
            .build();
        return dowmMessage;
    }

    private void sendAck(ChannelHandlerContext context, UpSingle upMessage) {
        MessageAck ackMessage = MessageAck.newBuilder().setId(upMessage.getId())
            .setTimestamp(upMessage.getTimestamp()).setStatus(AckMessageStatus.SUCCESS.getStatus())
            .build();
        context.channel().writeAndFlush(ackMessage);
    }


}
