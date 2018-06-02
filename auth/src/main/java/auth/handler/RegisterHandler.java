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
 * Created by win7 on 2016/3/2.
 */
public class RegisterHandler extends IMHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);

    public RegisterHandler(String userid, long netid, Message msg, ChannelHandlerContext ctx) {
        super(userid, netid, msg, ctx);
    }

    @Override
    protected void excute(Worker worker) throws TException {
        Auth.Register msg = (Auth.Register) this.msg;
        String username = msg.getUsername();
        String passwd = msg.getPasswd();
        Account account = new Account();
        account.setUserid(username);
        account.setPasswd(passwd);
        //todo 写数据库要加锁
        if (jedis.exists(UserUtils.genDBKey(username))) {
            RouteUtil.sendResponse(Common.ACCOUNT_DUMPLICATED, "Account already exists", netid,
                username);
            logger.info("Account already exists, userid: {}", username);
        } else {
            jedis.hset(UserUtils.genDBKey(username), UserUtils.userFileds.Account.field,
                DBOperator.Serialize(account));
            RouteUtil.sendResponse(Common.REGISTER_OK, "User registerd successd", netid, username);
            logger.info("User registerd successd, userid: {}", username);
        }

    }

}


