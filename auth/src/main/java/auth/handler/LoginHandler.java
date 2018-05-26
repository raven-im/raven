package auth.handler;

import auth.IMHandler;
import auth.Worker;
import auth.utils.Common;
import auth.utils.RouteUtil;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.protos.Auth;

/**
 * Created by win7 on 2016/3/3.
 */
public class LoginHandler extends IMHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    public LoginHandler(String userid, long netid, Message msg, ChannelHandlerContext ctx) {
        super(userid, netid, msg, ctx);
    }

    @Override
    protected void excute(Worker worker) throws TException {
        Auth.Login msg = (Auth.Login) this.msg;
        Account account;
        if (!jedis.exists(UserUtils.genDBKey(uid))) {
            RouteUtil.sendResponse(Common.ACCOUNT_INEXIST, "Account not exists", netid, uid);
            logger.info("Account not exists, userid: {}", uid);
            return;
        } else {
            byte[] userIdBytes = jedis
                    .hget(UserUtils.genDBKey(uid), UserUtils.userFileds.Account.field);
            account = DBOperator.Deserialize(new Account(), userIdBytes);
        }
        if (account.getUserid().equals(uid) && account.getPasswd().equals(msg.getPasswd())) {
            AuthServerHandler.putInUseridMap(uid, netid);
            RouteUtil.sendResponse(Common.VERYFY_PASSED, "Verify passed", netid, uid);
            logger.info("userid: {} verify passed", uid);
        } else {
            RouteUtil.sendResponse(Common.VERYFY_ERROR, "Account not exist or passwd error", netid,
                    uid);
            logger.info("userid: {} verify failed", uid);
        }
    }
}
