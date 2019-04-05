package com.tim.access.handler;

import com.google.protobuf.MessageLite;
import com.tim.access.channel.NettyChannelManager;
import com.tim.access.process.LoginAuthProcessor;
import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.LoginAck;
import com.tim.common.protos.Common.Code;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Sharable
@Slf4j
public class LoginAuthHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private LoginAuthProcessor loginAuthProcessor;

    @Autowired
    private NettyChannelManager nettyChannelManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof Login) {
            Login loginMesaage = (Login) messageLite;
            String token = loginMesaage.getToken();
            // TODO 校验token 增加路由
            nettyChannelManager.addUid2Channel(token, channelHandlerContext.channel());
            LoginAck loginAck = LoginAck.newBuilder()
                .setId(loginMesaage.getId())
                .setCode(Code.SUCCESS)
                .setTime(System.currentTimeMillis())
                .build();
            channelHandlerContext.writeAndFlush(loginAck);
        } else {
            channelHandlerContext.fireChannelRead(messageLite);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = nettyChannelManager.getUidByChannel(ctx.channel());
        if (null != uid) {
            log.info("client disconnected channelId:{},uid:{}", ctx.channel().id().asShortText(),
                uid);
            nettyChannelManager.removeChannel(ctx.channel());
            // TODO 清除路由
        }
    }

}

