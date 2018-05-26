package auth.handler;

import auth.HandlerManager;
import auth.IMHandler;
import auth.Worker;
import com.google.protobuf.Internal;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.utils.MessageProtoNum;
import protobuf.utils.Utils;
import protobuf.analysis.ParseMap;
import protobuf.protos.PrivateMessageProto;

/**
 * Created by win7 on 2016/3/5.
 */
public class AuthMessageConnectionHandler extends SimpleChannelInboundHandler<MessageLite> {

    private static final Logger logger = LoggerFactory
            .getLogger(AuthMessageConnectionHandler.class);

    private static ChannelHandlerContext _authLogicConnection;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        setAuthLogicConnecttion(ctx);
        logger.info("[Auth-Logic] connection is established");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageLite message)
            throws Exception {
        Internal.Transfer gt = (Internal.Transfer) message;
        int ptoNum = gt.getPtoNum();
        Message msg = ParseMap.getMessage(ptoNum, gt.getMsg().toByteArray());
        IMHandler handler = null;
        if (msg instanceof PrivateMessageProto.PrivateMessage) {
            handler = HandlerManager.getHandler(ptoNum, gt.getUserId(), -1L, msg,
                    AuthServerHandler.getGateAuthConnection());
        } else {
            logger.error("Error Messgae Type: {}", msg.getClass());
            return;
        }
        Worker.dispatch(gt.getUserId(), handler);
    }

    private void sendGreet2Logic() {
        Internal.Greet.Builder ig = Internal.Greet.newBuilder();
        ig.setFrom(Internal.Greet.From.Auth);
        ByteBuf out = Utils
                .pack2Server(ig.build(), MessageProtoNum.GREET, -1, Internal.Dest.Message, "admin");
        getAuthLogicConnection().writeAndFlush(out);
        logger.info("Auth send Green to Logic.");
    }

    private static ChannelHandlerContext getAuthLogicConnection() {
        return _authLogicConnection;
    }

    private static void setAuthLogicConnecttion(ChannelHandlerContext ctx) {
        _authLogicConnection = ctx;
    }
}
