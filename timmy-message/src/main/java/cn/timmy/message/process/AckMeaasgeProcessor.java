package cn.timmy.message.process;

import cn.timmy.common.enums.AckMessageStatus;
import cn.timmy.common.protos.Ack;
import cn.timmy.common.protos.Ack.AckMessage;
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

public class AckMeaasgeProcessor implements BaseMessageProcessor {

    @Autowired
    private OfflineMsgService offLineMsgService;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    private static final Logger logger = LogManager.getLogger(
        AckMeaasgeProcessor.class);

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        AckMessage ackMessage = (AckMessage) messageLite;
        String fromUid = nettyChannelManager.getUidByChannel(context.channel());
        logger.info("fromUid:{}", fromUid);
        offLineMsgService.deleteAckMessage(fromUid, ackMessage.getMsgid(),
            ackMessage.getSendtime());
    }


}
