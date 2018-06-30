package cn.timmy.message.process;

import cn.timmy.common.protos.MessageProto.DownStreamMessageProto;
import cn.timmy.common.protos.MessageProto.MsgType;
import cn.timmy.common.protos.MessageProto.UpStreamMessageProto;
import cn.timmy.common.utils.Constants;
import cn.timmy.common.utils.GsonHelper;
import cn.timmy.common.utils.ProtoConstants;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.common.BaseMessageProcessor;
import cn.timmy.message.server.TcpMessageServer;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 单聊消息
 * Date Created on 2018/6/2
 */
@Component
public class PrivateMessageProcessor implements BaseMessageProcessor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    private static final Logger logger = LogManager.getLogger(
        PrivateMessageProcessor.class);

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        UpStreamMessageProto upMessage = (UpStreamMessageProto) messageLite;
        List<String> uids = upMessage.getTouidList();
        String fromUid = nettyChannelManager.getUidByChannel(context.channel());
        logger.debug("fromUid:{}", fromUid);
        DownStreamMessageProto dowmMessage = DownStreamMessageProto.newBuilder()
            .setFromuid(fromUid)
            .setProtonum(ProtoConstants.DOWNPRIVATEMESSAGE)
            .setContent(upMessage.getContentBytes())
            .setMsgid(upMessage.getMsgid() == 0 ? TcpMessageServer.snowFlake.nextId()
                : upMessage.getMsgid())
            .setSendtime(upMessage.getSendtime())
            .setType(MsgType.PERSON)
            .build();
        uids.forEach(uid -> {
            List<Channel> channels = nettyChannelManager.getChannelByUid(uid);
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
        stringRedisTemplate.boundZSetOps(Constants.OFF_MSG_KEY + uid)
            .add(GsonHelper.getGson().toJson(downMessage), downMessage.getMsgid());
    }
}
