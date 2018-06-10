package message.process;

import com.google.protobuf.MessageLite;
import common.utils.Constants;
import common.utils.GsonHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import message.MessageStarter;
import message.channel.NettyChannelManager;
import message.common.BaseMessageProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import protobuf.protos.MessageProto.DownStreamMessageProto;
import protobuf.protos.MessageProto.MsgType;
import protobuf.protos.MessageProto.UpStreamMessageProto;
import protobuf.utils.ProtoConstants;
import redis.clients.jedis.Jedis;

/**
 * Author zxx
 * Description 单聊消息
 * Date Created on 2018/6/2
 */
public class PrivateMessageProcessor implements BaseMessageProcessor {

    private static final Logger logger = LogManager.getLogger(
        PrivateMessageProcessor.class);

    private static PrivateMessageProcessor privateMessageProcessor;

    public static synchronized PrivateMessageProcessor getInstance() {
        if (privateMessageProcessor == null) {
            privateMessageProcessor = new PrivateMessageProcessor();
        }
        return privateMessageProcessor;
    }

    private PrivateMessageProcessor() {
    }

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        UpStreamMessageProto upMessage = (UpStreamMessageProto) messageLite;
        List<String> uids = upMessage.getTouidList();
        String fromUid = NettyChannelManager.getInstance().getUidByChannel(context.channel());
        logger.debug("fromUid:{}", fromUid);
        DownStreamMessageProto dowmMessage = DownStreamMessageProto.newBuilder()
            .setFromuid(fromUid)
            .setProtonum(ProtoConstants.DOWNPRIVATEMESSAGE)
            .setContent(upMessage.getContentBytes())
            .setMsgid(upMessage.getMsgid() == 0 ? MessageStarter.SnowFlake.nextId()
                : upMessage.getMsgid())
            .setSendtime(upMessage.getSendtime())
            .setType(MsgType.PERSON)
            .build();
        uids.forEach(uid -> {
            List<Channel> channels = NettyChannelManager.getInstance().getChannelByUid(uid);
            if (null != channels) {
                storeOfflineMsg(dowmMessage, uid);
                channels
                    .forEach(channel -> channel.writeAndFlush(dowmMessage));
            } else {
                storeOfflineMsg(dowmMessage, uid);
            }
        });
    }

    private void storeOfflineMsg(DownStreamMessageProto downMessage, String uid) {
        Jedis jedis = MessageStarter.redisPoolManager.getJedis();
        jedis.zadd(Constants.OFF_MSG_KEY + uid, downMessage.getMsgid(),
            GsonHelper.getGson().toJson(downMessage));
    }
}
