package auth;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.apache.thrift.TException;
import redis.clients.jedis.Jedis;

/**
 * Created by Dell on 2016/3/2.
 */
public abstract class IMHandler {

    protected  final String userid;
    protected  final long netid;
    protected  final Message msg;
    protected ChannelHandlerContext ctx;
    protected Jedis jedis;

    public IMHandler(String userid, long netid, Message msg,ChannelHandlerContext ctx) {
        this.userid = userid;
        this.netid = netid;
        this.msg = msg;
        this.ctx = ctx;
    }

    protected abstract void excute(Worker worker) throws TException;
}
