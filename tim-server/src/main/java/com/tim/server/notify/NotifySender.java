package com.tim.server.notify;

import com.tim.common.mqmsg.BaseMessage;
import com.tim.common.protos.Notify.NotifyMessage;
import com.tim.common.utils.DateTimeUtils;
import com.tim.common.utils.ProtoConstants;
import com.tim.server.channel.NettyChannelManager;
import com.tim.server.common.OfflineMsgService;
import com.tim.server.server.TcpMessageServer;
import io.netty.channel.Channel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotifySender {

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Autowired
    private OfflineMsgService offLineMsgService;

    public void sendNotifyMsg(BaseMessage baseMessage) {
        NotifyMessage notifyMessage = NotifyMessage.newBuilder()
            .setFromuid(baseMessage.getFrom_uid())
            .setProtonum(ProtoConstants.NOTIFY)
            .setContent(baseMessage.getContent())
            .setMsgid(String.valueOf(TcpMessageServer.snowFlake.nextId()))
            .setSendtime(DateTimeUtils.currentUTC().getTime())
            .setType(baseMessage.getType())
            .build();
        List<Channel> channels = nettyChannelManager.getChannelByUid(baseMessage.getTo_uid());
        if (channels.isEmpty()) {
            offLineMsgService.storeOfflineNotify(baseMessage.getTo_uid(), notifyMessage,
                notifyMessage.getMsgid(),notifyMessage.getSendtime());
        } else {
            channels
                .forEach(channel -> channel.writeAndFlush(notifyMessage));
        }
    }

}
