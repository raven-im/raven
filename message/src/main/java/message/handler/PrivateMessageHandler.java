package message.handler;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import message.IMHandler;
import message.Worker;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.MessageProtoNum;
import protobuf.Utils;
import protobuf.generate.cli2srv.chat.Chat;
import protobuf.generate.cli2srv.login.Auth;
import protobuf.generate.internal.Internal;

/**
 * Created by win7 on 2016/3/5.
 */
public class PrivateMessageHandler extends IMHandler {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageHandler.class);

    public PrivateMessageHandler(String userid, long netid, Message msg, ChannelHandlerContext ctx) {
        super(userid, netid, msg, ctx);
    }

    @Override
    protected void excute(Worker worker) throws TException {
        Chat.CPrivateChat msg = (Chat.CPrivateChat) _msg;
        ByteBuf byteBuf = null;
        //转发给auth
        byteBuf = Utils
                .pack2Server(_msg, MessageProtoNum.CPRIVATECHAT, Internal.Dest.Auth, msg.getDest());
        MessageServerHandler.getAuthMessageConnection().writeAndFlush(byteBuf);
        //给发消息的人回应
        Auth.SResponse.Builder sr = Auth.SResponse.newBuilder();
        sr.setCode(300);
        sr.setDesc("Server received message successed");
        byteBuf = Utils.pack2Client(sr.build());
        _ctx.writeAndFlush(byteBuf);
    }
}
