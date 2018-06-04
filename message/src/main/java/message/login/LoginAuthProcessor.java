package message.login;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import message.MessageStarter;
import message.channel.NettyChannelManager;
import message.common.BaseMessageProcessor;
import protobuf.protos.Auth.Login;
import protobuf.protos.Auth.Response;
import protobuf.protos.ResponseEnum;
import protobuf.utils.ProtoConstants;

/**
 * Author zxx
 * Description 登录验证
 * Date Created on 2018/6/2
 */
public class LoginAuthProcessor implements BaseMessageProcessor {

    private static LoginAuthProcessor loginAuth;

    public static synchronized LoginAuthProcessor getInstance() {
        if (loginAuth == null) {
            loginAuth = new LoginAuthProcessor();
        }
        return loginAuth;
    }

    private LoginAuthProcessor() {
    }

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        String token = ((Login) messageLite).getToken();
        // todo 校验token
        pulishMsg(ResponseEnum.SUCCESS, context.channel());
        NettyChannelManager.getInstance().addUid2Channel(token, context.channel());
    }

    private void pulishMsg(ResponseEnum responseEnum, Channel channel) {
        Response response = Response.newBuilder()
            .setCode(responseEnum.code)
            .setMsg(responseEnum.msg)
            .setProtonum(ProtoConstants.RESPONSE)
            .setMsgid(MessageStarter.SnowFlake.nextId())
            .build();
        channel.writeAndFlush(response);
    }

}
