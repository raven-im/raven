package cn.timmy.message.process;

import cn.timmy.common.protos.Auth.Login;
import cn.timmy.common.protos.Auth.Response;
import cn.timmy.common.utils.ProtoConstants;
import cn.timmy.message.channel.NettyChannelManager;
import cn.timmy.message.common.BaseMessageProcessor;
import cn.timmy.message.common.ResponseEnum;
import cn.timmy.message.server.TcpMessageServer;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 登录验证
 * Date Created on 2018/6/2
 */
@Component
public class LoginAuthProcessor implements BaseMessageProcessor {

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void process(MessageLite messageLite, ChannelHandlerContext context) {
        String token = ((Login) messageLite).getToken();
        // todo 校验token
        nettyChannelManager.addUid2Channel(token, context.channel());
        pulishMsg(ResponseEnum.SUCCESS, context.channel());
    }

    private void pulishMsg(ResponseEnum responseEnum, Channel channel) {
        Response response = Response.newBuilder()
            .setCode(responseEnum.code)
            .setMsg(responseEnum.msg)
            .setProtonum(ProtoConstants.RESPONSE)
            .setMsgid(TcpMessageServer.snowFlake.nextId())
            .build();
        channel.writeAndFlush(response);
    }

}
