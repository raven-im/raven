package com.raven.route.message.processor;

import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.NotifyMessage;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.storage.route.RouteManager;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotifyUserProcessor implements Runnable {

    private ServerChannelManager gateWayServerChannelManager;

    private RouteManager routeManager;

    private String notification;

    public NotifyUserProcessor(ServerChannelManager gateWayServerChannelManager,
        RouteManager routeManager, String notification) {
        this.gateWayServerChannelManager = gateWayServerChannelManager;
        this.routeManager = routeManager;
        this.notification = notification;
    }

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(notification, builder);
        NotifyMessage notifyMessage = builder.getNotifyMessage();

        String userId = notifyMessage.getTargetUid();

        GatewayServerInfo server = routeManager.getServerByUid(userId);
        if (null != server) {
            Channel channel = gateWayServerChannelManager.getChannelByServer(server);
            if (channel != null) {
                NotifyMessage downNotification = NotifyMessage.newBuilder().mergeFrom(notifyMessage)
                    .setTargetUid(userId).build();
                RavenMessage ravenMessage = RavenMessage.newBuilder()
                    .setType(Type.NotifyMessage)
                    .setNotifyMessage(downNotification).build();
                channel.writeAndFlush(ravenMessage);
            } else {
                log.error("cannot find channel. server:{}", server);
            }
        } else {
            log.info("uid:{} no server to push down msg:{}.", userId, notifyMessage.getId());
        }

    }
}
