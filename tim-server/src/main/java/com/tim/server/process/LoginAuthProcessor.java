package com.tim.server.process;

import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.LoginAck;
import com.tim.server.channel.NettyChannelManager;
import com.tim.server.common.BaseMessageProcessor;
import com.tim.server.common.ResponseEnum;
import com.tim.server.server.BaseTcpMessageServer;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        LoginAck response = LoginAck.newBuilder()
            .setCode(responseEnum.code)
            .setMsg(responseEnum.msg)
            .setId(String.valueOf(BaseTcpMessageServer.snowFlake.nextId()))
            .build();
        channel.writeAndFlush(response);
    }

}
