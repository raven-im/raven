package com.raven.gateway.handler;

import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.netty.NettyAttrUtil;
import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.Login;
import com.raven.common.protos.Message.LoginAck;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.storage.route.RouteManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Sharable
@Slf4j
public class AuthenticationHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RouteManager routeManager;

    @Autowired
    private ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

    @Value("${netty.tcp.port}")
    private int tcpPort;

    @Value("${netty.websocket.port}")
    private int wsPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected remote address:{}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {
        NettyAttrUtil.updateReaderTime(ctx.channel(), System.currentTimeMillis());
        if (message.getType() == Type.Login) {
            Login loginMessage = message.getLogin();
            log.info("login msg:{}", JsonHelper.toJsonString(loginMessage));
            String token = loginMessage.getToken();
            if (!verifyToken(token)) {
                LoginAck loginAck = LoginAck.newBuilder()
                        .setId(loginMessage.getId())
                        .setCode(Code.TOKEN_INVALID)
                        .setTime(System.currentTimeMillis())
                        .build();
                ctx.writeAndFlush(loginAck);
            }
            routeManager.addUser2Server(loginMessage.getUid(),
                    new GatewayServerInfo(zookeeperDiscoveryProperties.getInstanceHost(), tcpPort, wsPort));
            uidChannelManager.addId2Channel(loginMessage.getUid(), ctx.channel());
            sendLoginAck(ctx, loginMessage.getId(), Code.SUCCESS);
        } else {
            if (null == uidChannelManager.getIdByChannel(ctx.channel())) {
                ctx.close();
            }
            ctx.fireChannelRead(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        if (null != uid) {
            log.info("client disconnected uid:{}", uid);
            uidChannelManager.removeChannel(ctx.channel());
            // 最后一台设备下线才清除路由
            if (CollectionUtils.isEmpty(uidChannelManager.getChannelsById(uid))) {
                routeManager.removerUserFromServer(uid,
                        new GatewayServerInfo(zookeeperDiscoveryProperties.getInstanceHost(), tcpPort, wsPort));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

    private boolean verifyToken(String token) {
        // TODO 验证UID
        return redisTemplate.hasKey(token);
    }

    private void sendLoginAck(ChannelHandlerContext ctx, long id, Code code) {
        LoginAck loginAck = LoginAck.newBuilder()
                .setId(id)
                .setCode(code)
                .setTime(System.currentTimeMillis())
                .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.LoginAck)
                .setLoginAck(loginAck).build();
        ctx.writeAndFlush(ravenMessage);
    }


}

