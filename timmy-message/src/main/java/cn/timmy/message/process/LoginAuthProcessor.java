package cn.timmy.message.process;

import cn.timmy.message.MessageStarter;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.common.BaseMessageProcessor;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import cn.timmy.proto.protos.Auth.Login;
import cn.timmy.proto.protos.Auth.Response;
import cn.timmy.proto.protos.ResponseEnum;
import cn.timmy.proto.utils.ProtoConstants;

/**
 * Author zxx
 * Description 登录验证
 * Date Created on 2018/6/2
 */
public class LoginAuthProcessor implements BaseMessageProcessor {

    private static LoginAuthProcessor loginAuthProcessor;

    public static synchronized LoginAuthProcessor getInstance() {
        if (loginAuthProcessor == null) {
            loginAuthProcessor = new LoginAuthProcessor();
        }
        return loginAuthProcessor;
    }

    private LoginAuthProcessor() {
    }

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        String token = ((Login) messageLite).getToken();
        // todo 校验token
        NettyChannelManager.getInstance().addUid2Channel(token, context.channel());
        pulishMsg(ResponseEnum.SUCCESS, context.channel());
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
