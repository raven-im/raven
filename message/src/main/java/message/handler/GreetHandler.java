package message.handler;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import message.IMHandler;
import message.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.generate.internal.Internal;

/**
 * Created by win7 on 2016/3/5.
 */
public class GreetHandler extends IMHandler {

    private static final Logger logger = LoggerFactory.getLogger(GreetHandler.class);

    public GreetHandler(String userid, long netid, Message msg, ChannelHandlerContext ctx) {
        super(userid, netid, msg, ctx);
    }

    @Override
    protected void excute(Worker worker) {
        Internal.Greet msg = (Internal.Greet) _msg;
        Internal.Greet.From from = msg.getFrom();

        if (from == Internal.Greet.From.Auth) {
            MessageServerHandler.setAuthMessageConnection(_ctx);
            logger.info("[Auth-Logic] connection is established");
        } else if (from == Internal.Greet.From.Gate) {
            MessageServerHandler.setGateLogicConnection(_ctx);
            logger.info("[Gate-Logic] connection is established");

        }
    }
}
