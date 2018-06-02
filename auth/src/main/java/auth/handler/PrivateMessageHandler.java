package auth.handler;

import auth.IMHandler;
import auth.Worker;
import com.google.protobuf.Internal;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.PrivateMessageProto;
import protobuf.utils.MessageProtoNum;
import protobuf.utils.Utils;

/**
 * Created by win7 on 2016/3/5.
 */
public class PrivateMessageHandler extends IMHandler {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageHandler.class);

    public PrivateMessageHandler(String userid, long netid, Message msg,
        ChannelHandlerContext ctx) {
        super(userid, netid, msg, ctx);
    }

    @Override
    protected void excute(Worker worker) {
        PrivateMessageProto.PrivateMessage msg = (PrivateMessageProto.PrivateMessage) this.msg;
        ByteBuf byteBuf;
        String dest = msg.getToUId();
        Long netid = AuthServerHandler.getNetidByUserid(dest);
        if (netid == null) {
            logger.error("Dest User not online");
            return;
        }
        PrivateMessageProto.PrivateMessage.Builder sp = PrivateMessageProto.PrivateMessage
            .newBuilder();
        sp.setContent(msg.getContent());
        byteBuf = Utils
            .pack2Server(sp.build(), MessageProtoNum.PRIVATEMESSAGE, netid, Internal.Dest.Gate,
                dest);
        ctx.writeAndFlush(byteBuf);
        logger.info("message has send from {} to {}", msg.getFromUId(), msg.getToUId());
    }
}
