package cn.timmy.message.process;

import cn.timmy.common.protos.Message.DownStreamMessage;
import cn.timmy.common.protos.Message.MsgType;
import cn.timmy.common.protos.Message.UpStreamMessage;
import cn.timmy.common.utils.ProtoConstants;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.common.BaseMessageProcessor;
import cn.timmy.message.common.OfflineMsgService;
import cn.timmy.message.server.TcpMessageServer;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 单聊消息
 * Date Created on 2018/6/2
 */
@Component
public class PrivateMessageProcessor implements BaseMessageProcessor {

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    private static final Logger logger = LogManager.getLogger(
        PrivateMessageProcessor.class);

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        UpStreamMessage upMessage = (UpStreamMessage) messageLite;
        List<String> uids = upMessage.getTouidList();
        String fromUid = nettyChannelManager.getUidByChannel(context.channel());
        logger.debug("fromUid:{}", fromUid);
        DownStreamMessage dowmMessage = DownStreamMessage.newBuilder()
            .setFromuid(fromUid)
            .setProtonum(ProtoConstants.DOWNSTREAMMESSAGE)
            .setContent(upMessage.getContent())
            .setMsgid(TcpMessageServer.snowFlake.nextId())
            .setSendtime(upMessage.getSendtime())
            .setType(MsgType.PERSON)
            .build();
        uids.forEach(uid -> {
            List<Channel> channels = nettyChannelManager.getChannelByUid(uid);
            if (channels.isEmpty()) {
                offLineMsgService.storeOfflineMsg(uid, dowmMessage, dowmMessage.getMsgid());
            } else {
                channels
                    .forEach(channel -> channel.writeAndFlush(dowmMessage));
            }
        });
    }

}
