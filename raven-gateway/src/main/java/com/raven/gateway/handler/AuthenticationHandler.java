package com.raven.gateway.handler;

import com.raven.common.exception.TokenException;
import com.raven.common.exception.TokenExceptionType;
import com.raven.common.model.Token;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.netty.NettyAttrUtil;
import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.Login;
import com.raven.common.protos.Message.LoginAck;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.raven.common.netty.NettyAttrUtil.*;
import static com.raven.common.utils.Constants.DEFAULT_SEPARATOR;

@Component
@Sharable
@Slf4j
public class AuthenticationHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Value("${netty.tcp.port}")
    private int tcpPort;

    @Value("${netty.websocket.port}")
    private int wsPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("client connected remote address:{}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {
        if (message.getType() == Type.Login) {
            Login loginMessage = message.getLogin();
            log.debug("login msg:{}", JsonHelper.toJsonString(loginMessage));

            try {
                Token token = Token.parseFromString(loginMessage.getToken());
                log.info("AppKey [{}], User[{}], Device[{}] login.", token.getAppKey(), token.getUid(), token.getDeviceId());
                //set channel attributes.
                NettyAttrUtil.setAttrKey(ctx.channel(), ATTR_KEY_APP_KEY, token.getAppKey());
                NettyAttrUtil.setAttrKey(ctx.channel(), ATTR_KEY_USER_ID, token.getUid());
                NettyAttrUtil.setAttrKey(ctx.channel(), ATTR_KEY_DEVICE_ID, token.getDeviceId());
                NettyAttrUtil.setAttrKey(ctx.channel(), ATTR_KEY_LOGIN_TIME, String.valueOf(System.currentTimeMillis()));

                String uid = token.getAppKey() + DEFAULT_SEPARATOR + token.getUid();
                uidChannelManager.addUid2Channel(uid, ctx.channel(), token.getDeviceId());
                sendLoginAck(ctx, loginMessage.getId(), Code.SUCCESS);
            } catch (TokenException e) {
                if (TokenExceptionType.TOKEN_INVALID == e.getType()) {
                    sendLoginAck(ctx, loginMessage.getId(), Code.TOKEN_INVALID);
                } else if (TokenExceptionType.TOKEN_EXPIRE == e.getType()) {
                    sendLoginAck(ctx, loginMessage.getId(), Code.TOKEN_EXPIRE);
                }
            }
        } else {
            if (null == uidChannelManager.getUidByChannel(ctx.channel())) {
                ctx.close();
            }
            ctx.fireChannelRead(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = uidChannelManager.getUidByChannel(ctx.channel());
        if (null != uid) {
            String appKey = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_APP_KEY);
            String userId = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_USER_ID);
            String deviceId = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_DEVICE_ID);
            String loginTime = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_LOGIN_TIME);
            long eclipse = (System.currentTimeMillis() - Long.parseLong(loginTime)) / 1000L;
            log.info("AppKey [{}], User[{}], Device[{}] disconnected. connection time [{}] seconds",
                    appKey, userId, deviceId, eclipse);
            uidChannelManager.removeChannel(ctx.channel());
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    private void sendLoginAck(ChannelHandlerContext ctx, long id, Code code) {
        LoginAck loginAck = LoginAck.newBuilder()
                .setId(id)
                .setCode(code)
                .setTime(System.currentTimeMillis())
                .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder()
                .setType(Type.LoginAck)
                .setLoginAck(loginAck)
                .build();
        ctx.writeAndFlush(ravenMessage);
    }


}

