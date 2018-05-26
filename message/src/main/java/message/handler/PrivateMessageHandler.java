package message.handler;

import com.google.protobuf.MessageLite;
import common.connection.Connection;
import common.connection.ConnectionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.PrivateMessageProto;
import protobuf.protos.PrivateMessageProto.MsgType;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx
 * Description 私聊消息handler
 * Date Created on 2018/5/25
 */
public class PrivateMessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageHandler.class);

    private final ConnectionManager connectionManager;

    public PrivateMessageHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
            MessageLite messageLite) throws Exception {
        if (messageLite instanceof PrivateMessageProto.UpStreamMessageProto) {
            PrivateMessageProto.UpStreamMessageProto upMessage = (PrivateMessageProto.UpStreamMessageProto) messageLite;
            List<String> uids = upMessage.getTouidList();
            String fromUid = connectionManager.getConnection(channelHandlerContext.channel()).getUid();
            logger.debug("fromUid:{}", fromUid);
            MessageLite dowmMessage = PrivateMessageProto.DownStreamMessageProto.newBuilder()
                    .setFromuid(fromUid)
                    .setProtonum(ProtoConstants.DOWNPRIVATEMESSAGE)
                    .setContent(upMessage.getContentBytes())
                    .setMsgid(UUID.randomUUID().toString())
                    .setSendtime(upMessage.getSendtime())
                    .setType(MsgType.PERSON)
                    .build();
            uids.forEach(uid -> {
                List<Connection> connections = connectionManager.getConnectionByUid(uid);
                if (null != connections) {
                    connections
                            .forEach(connection -> connection.getChannel().writeAndFlush(dowmMessage));
                }
            });
        }
    }
}
