package message.handler;

import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import message.Worker;
import org.apache.thrift.TException;

/**
 * Created by Dell on 2016/3/2.
 */
public abstract class IMHandler {
    protected final String userid;
    protected final long  netid;
    protected final MessageLite msg;
    protected ChannelHandlerContext ctx;

    protected IMHandler(String userid, long netid, MessageLite msg, ChannelHandlerContext ctx) {
        this.userid = userid;
        this.netid = netid;
        this.msg = msg;
        this.ctx = ctx;
    }

    public abstract void excute(Worker worker) throws TException;
}
