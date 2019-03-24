package com.tim.server.process;

import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.Response;
import com.tim.common.utils.ProtoConstants;
import com.tim.server.channel.NettyChannelManager;
import com.tim.server.common.BaseMessageProcessor;
import com.tim.server.common.ResponseEnum;
import com.tim.server.server.TcpMessageServer;
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
        Response response = Response.newBuilder()
            .setCode(responseEnum.code)
            .setMsg(responseEnum.msg)
            .setProtonum(ProtoConstants.RESPONSE)
            .setMsgid(String.valueOf(TcpMessageServer.snowFlake.nextId()))
            .build();
        channel.writeAndFlush(response);
    }

}
