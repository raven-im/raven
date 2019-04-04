package com.tim.group.tcp.process;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.LoginAck;
import com.tim.group.tcp.channel.NettyChannelManager;
import com.tim.group.tcp.common.BaseMessageProcessor;
import com.tim.group.tcp.common.ResponseEnum;
import com.tim.group.tcp.server.GroupTcpMessageServer;
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
    }

}
