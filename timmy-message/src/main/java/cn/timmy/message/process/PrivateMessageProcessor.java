package cn.timmy.message.process;

import cn.timmy.common.enums.AckMessageStatus;
import cn.timmy.common.protos.Ack.AckMessage;
import cn.timmy.common.protos.Message.DownStreamMessage;
import cn.timmy.common.protos.Message.MsgType;
import cn.timmy.common.protos.Message.UpStreamMessage;
import cn.timmy.common.utils.ParseRegistryMap;
import cn.timmy.common.utils.ProtoConstants;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.common.BaseMessageProcessor;
import cn.timmy.message.common.OfflineMsgService;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrivateMessageProcessor implements BaseMessageProcessor {

    private static final Logger logger = LogManager.getLogger(
        PrivateMessageProcessor.class);

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        UpStreamMessage upMessage = (UpStreamMessage) messageLite;
        senAck(context, upMessage);
        DownStreamMessage dowmMessage = buildMessage(context, upMessage);
        List<String> uids = upMessage.getTouidList();
        uids.forEach(uid -> {
            List<Channel> channels = nettyChannelManager.getChannelByUid(uid);
            if (channels.isEmpty()) {
                offLineMsgService.storeOfflineMsg(uid, dowmMessage, dowmMessage.getMsgid(),
                    dowmMessage.getSendtime());
            } else {
                offLineMsgService.storeWaitAckMessage(uid, dowmMessage, dowmMessage.getMsgid(),
                    dowmMessage.getSendtime());
                channels.forEach(channel ->
                    channel.writeAndFlush(dowmMessage).addListener(future -> {
                        if (!future.isSuccess()) {
                            offLineMsgService
                                .storeOfflineMsg(uid, dowmMessage, dowmMessage.getMsgid(),
                                    dowmMessage.getSendtime());
                        }
                    }));
            }
        });
    }

    private DownStreamMessage buildMessage(ChannelHandlerContext context,
        UpStreamMessage upMessage) {
        String fromUid = nettyChannelManager.getUidByChannel(context.channel());
        DownStreamMessage dowmMessage = DownStreamMessage.newBuilder()
            .setFromuid(fromUid)
            .setProtonum(ProtoConstants.DOWNSTREAMMESSAGE)
            .setContent(upMessage.getContent())
            .setMsgid(upMessage.getMsgid())
            .setSendtime(upMessage.getSendtime())
            .setType(MsgType.PERSON)
            .build();
        return dowmMessage;
    }

    private void senAck(ChannelHandlerContext context, UpStreamMessage upMessage) {
        AckMessage ackMessage = AckMessage.newBuilder().setMsgid(upMessage.getMsgid())
            .setSendtime(upMessage.getSendtime()).setStatus(AckMessageStatus.SUCCESS.getStatus())
            .build();
        context.channel().writeAndFlush(ackMessage);
    }


}
