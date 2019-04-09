package com.tim.access.handler;


import com.google.protobuf.MessageLite;

import com.tim.common.netty.ServerChannelManager;
import com.tim.common.protos.Auth.Login;
import com.tim.common.protos.Auth.LoginAck;
import com.tim.common.protos.Common.Code;
import com.tim.common.utils.Constants;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

@Sharable
@Slf4j
public class LoginAuthHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Autowired
    private ServerChannelManager uidChannelManager;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${netty.server.port}")
    private int nettyServerPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected remote address:{},id:{}", ctx.channel().remoteAddress(),
            ctx.channel().id().asShortText());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof Login) {
            Login loginMesaage = (Login) messageLite;
            String token = loginMesaage.getToken();
            if (!verifyToken(token)) {
                LoginAck loginAck = LoginAck.newBuilder()
                    .setId(loginMesaage.getId())
                    .setCode(Code.FAIL)
                    .setTime(System.currentTimeMillis())
                    .build();
                ctx.writeAndFlush(loginAck);
            }
            // 增加路由
            redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY)
                .putIfAbsent(loginMesaage.getUid(), getLocalAddress());
            redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + getLocalAddress())
                .add(loginMesaage.getUid());
            uidChannelManager.addId2Channel(token, ctx.channel());
            LoginAck loginAck = LoginAck.newBuilder()
                .setId(loginMesaage.getId())
                .setCode(Code.SUCCESS)
                .setTime(System.currentTimeMillis())
                .build();
            ctx.writeAndFlush(loginAck);
        } else {
            if (null == uidChannelManager.getIdByChannel(ctx.channel())) {
                ctx.close();
            }
            ctx.fireChannelRead(messageLite);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        if (null != uid) {
            log.info("client disconnected channelId:{},uid:{}", ctx.channel().id().asShortText(),
                uid);
            uidChannelManager.removeChannel(ctx.channel());
            // 最后一台设备下线才清除路由
            if (CollectionUtils.isEmpty(uidChannelManager.getChannelsById(uid))) {
                redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).delete(uid);
                redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + getLocalAddress())
                    .remove(uid);
            }
        }
    }

    private String getLocalAddress() throws SocketException {
        String address = null;
        Collection<InetAddress> ips = ServiceInstanceBuilder.getAllLocalIPs();
        if (ips.size() > 0) {
            address = ips.iterator().next().getHostAddress();   // 参考zk注册代码
        }
        address = address + ":" + nettyServerPort;
        return address;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

    private boolean verifyToken(String token) {
        return redisTemplate.hasKey(token);
    }


}

