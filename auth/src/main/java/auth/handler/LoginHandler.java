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
import protobuf.generate.cli2srv.login.Auth;
import tools.redis.utils.UserUtils;
import tools.thrift.generate.db.user.Account;
import tools.thrift.utils.DBOperator;

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
        Auth.CLogin msg = (Auth.CLogin) msg;
        Account account;
        if (!jedis.exists(UserUtils.genDBKey(userid))) {
            RouteUtil.sendResponse(Common.ACCOUNT_INEXIST, "Account not exists", netid, userid);
            logger.info("Account not exists, userid: {}", userid);
            return;
        } else {
            byte[] userIdBytes = jedis
                    .hget(UserUtils.genDBKey(userid), UserUtils.userFileds.Account.field);
            account = DBOperator.Deserialize(new Account(), userIdBytes);
        }
        if (account.getUserid().equals(userid) && account.getPasswd().equals(msg.getPasswd())) {
            AuthServerHandler.putInUseridMap(userid, netid);
            RouteUtil.sendResponse(Common.VERYFY_PASSED, "Verify passed", netid, userid);
            logger.info("userid: {} verify passed", userid);
        } else {
            RouteUtil.sendResponse(Common.VERYFY_ERROR, "Account not exist or passwd error", netid,
                    userid);
            logger.info("userid: {} verify failed", userid);
        }
    }
}
