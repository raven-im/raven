package cn.timmy.message.notify;

import cn.timmy.common.mqmsg.BaseMessage;
import cn.timmy.common.protos.Notify.NotifyMessage;
import cn.timmy.common.utils.DateTimeUtils;
import cn.timmy.common.utils.ProtoConstants;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.common.OfflineMsgService;
import cn.timmy.message.server.TcpMessageServer;
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
