package message.handler;

import com.google.protobuf.MessageLite;
import common.connection.Connection;
import common.connection.ConnectionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.List;
import java.util.UUID;
import protobuf.protos.PrivateMessageProto;
import protobuf.protos.PrivateMessageProto.MsgType;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx
 * Description 私聊消息handler
 * Date Created on 2018/5/25
 */
public class PrivateMessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    private final ConnectionManager connectionManager;

    public PrivateMessageHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
            MessageLite messageLite) throws Exception {
        if (messageLite instanceof PrivateMessageProto.UpStreamMessageProto) {
            PrivateMessageProto.UpStreamMessageProto upMessage = (PrivateMessageProto.UpStreamMessageProto) messageLite;
            List<String> uids = upMessage.getToUIdList();
            String fromUid = connectionManager.get(channelHandlerContext.channel()).getUid();
            MessageLite dowmMessage = PrivateMessageProto.DownStreamMessageProto.newBuilder()
                    .setFromUserId(fromUid)
                    .setProtoNum(ProtoConstants.DOWNPRIVATEMESSAGE)
                    .setContent(upMessage.getContentBytes())
                    .setMsgId(UUID.randomUUID().toString())
                    .setSendtime(upMessage.getSendtiime())
                    .setType(MsgType.PERSON)
                    .build();
            uids.forEach(uid -> {
                List<Connection> connections = connectionManager.getConnectionByUid(uid);
                connections
                        .forEach(connection -> connection.getChannel().writeAndFlush(dowmMessage));
            });
        }
    }
}
