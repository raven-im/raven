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
 * Created by win7 on 2016/3/2.
 */
public class RegisterHandler extends IMHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);

    public RegisterHandler(String userid, long netid, Message msg, ChannelHandlerContext ctx) {
        super(userid, netid, msg, ctx);
    }

    @Override
    protected void excute(Worker worker) throws TException {
        Auth.CRegister msg = (Auth.CRegister) this.msg;
        String userid = msg.getUserid();
        String passwd = msg.getPasswd();
        Account account = new Account();
        account.setUserid(userid);
        account.setPasswd(passwd);
        //todo 写数据库要加锁
        if (jedis.exists(UserUtils.genDBKey(userid))) {
            RouteUtil.sendResponse(Common.ACCOUNT_DUMPLICATED, "Account already exists", netid,
                    userid);
            logger.info("Account already exists, userid: {}", userid);
        } else {
            jedis.hset(UserUtils.genDBKey(userid), UserUtils.userFileds.Account.field,
                    DBOperator.Serialize(account));
            RouteUtil.sendResponse(Common.REGISTER_OK, "User registerd successd", netid, userid);
            logger.info("User registerd successd, userid: {}", userid);
        }

    }

}


