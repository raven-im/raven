package com.tim.access.process;

import com.google.protobuf.MessageLite;
import com.tim.access.channel.NettyChannelManager;
import com.tim.common.enums.LoginResult;
import com.tim.common.netty.BaseMessageProcessor;
import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.LoginAck;
import com.tim.common.protos.Common;
import com.tim.common.protos.Common.Code;
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

    }


}
