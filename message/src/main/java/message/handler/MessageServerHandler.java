package message.handler;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.HandlerManager;
import message.IMHandler;
import message.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.analysis.ParseMap;
import protobuf.generate.internal.Internal;

/**
 * Created by Dell on 2016/2/18.
 */


public class MessageServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(MessageServerHandler.class);
    private static ChannelHandlerContext gateLogicConnection;
    private static ChannelHandlerContext authLogicConnection;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message)
            throws Exception {
        Internal.GTransfer gt = (Internal.GTransfer) message;
        int ptoNum = gt.getPtoNum();
        Message msg = ParseMap.getMessage(ptoNum, gt.getMsg().toByteArray());
        IMHandler handler;
        if (msg instanceof Internal.Greet) {
            handler = HandlerManager
                    .getHandler(ptoNum, gt.getUserId(), gt.getNetId(), msg, channelHandlerContext);
        } else {
            handler = HandlerManager.getHandler(ptoNum, gt.getUserId(), gt.getNetId(), msg,
                    getGateLogicConnection());
        }
        Worker.dispatch(gt.getUserId(), handler);
    }

    public static void setGateLogicConnection(ChannelHandlerContext ctx) {
        gateLogicConnection = ctx;
    }

    public static ChannelHandlerContext getGateLogicConnection() {
        if (gateLogicConnection != null) {
            return gateLogicConnection;
        } else {
            return null;
        }
    }

    public static void setAuthMessageConnection(ChannelHandlerContext ctx) {
        authLogicConnection = ctx;
    }

    public static ChannelHandlerContext getAuthMessageConnection() {
        if (authLogicConnection != null) {
            return authLogicConnection;
        } else {
            return null;
        }
    }
}

