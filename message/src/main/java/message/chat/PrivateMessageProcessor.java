package message.chat;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import message.MessageStarter;
import message.channel.NettyChannelManager;
import message.common.BaseMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.MessageProto.DownStreamMessageProto;
import protobuf.protos.MessageProto.MsgType;
import protobuf.protos.MessageProto.UpStreamMessageProto;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx
 * Description 单聊消息
 * Date Created on 2018/6/2
 */
public class PrivateMessageProcessor implements BaseMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageProcessor.class);

    private static PrivateMessageProcessor privateMessageProcessor;

    public static synchronized PrivateMessageProcessor getInstance() {
        if (privateMessageProcessor == null) {
            privateMessageProcessor = new PrivateMessageProcessor();
        }
        return privateMessageProcessor;
    }

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        UpStreamMessageProto upMessage = (UpStreamMessageProto) messageLite;
        List<String> uids = upMessage.getTouidList();
        String fromUid = NettyChannelManager.getInstance().getUidByChannel(context.channel());
        logger.debug("fromUid:{}", fromUid);
        MessageLite dowmMessage = DownStreamMessageProto.newBuilder()
            .setFromuid(fromUid)
            .setProtonum(ProtoConstants.DOWNPRIVATEMESSAGE)
            .setContent(upMessage.getContentBytes())
            .setMsgid(MessageStarter.SnowFlake.nextId())
            .setSendtime(upMessage.getSendtime())
            .setType(MsgType.PERSON)
            .build();
        uids.forEach(uid -> {
            List<Channel> channels = NettyChannelManager.getInstance().getChannelByUid(uid);
            if (null != channels) {
                channels
                    .forEach(channel -> channel.writeAndFlush(dowmMessage));
            } else {
                // TODO 离线消息存储
            }
        });
    }

    @Override
    public void storeOfflineMsg(MessageLite messageLite, String uid) {

    }
}
