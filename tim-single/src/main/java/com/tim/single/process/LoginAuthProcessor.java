package com.tim.single.process;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.LoginAck;
import com.tim.single.channel.NettyChannelManager;
import com.tim.single.common.BaseMessageProcessor;
import com.tim.single.common.ResponseEnum;
import com.tim.single.server.SingleTcpMessageServer;
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
        publishMsg(ResponseEnum.SUCCESS, context.channel());
    }

    private void publishMsg(ResponseEnum responseEnum, Channel channel) {
        LoginAck response = LoginAck.newBuilder()
            .setCode(responseEnum.code)
            .setMsg(responseEnum.msg)
            .setId(String.valueOf(SingleTcpMessageServer.snowFlake.nextId()))
            .build();
        channel.writeAndFlush(response);
    }

}
