package com.raven.route.message.processor;

import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.NotifyMessage;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import io.netty.channel.Channel;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotifyConvProcessor implements Runnable {

    private ServerChannelManager gateWayServerChannelManager;

    private ConverManager converManager;

    private RouteManager routeManager;

    private String notification;

    public NotifyConvProcessor(ServerChannelManager gateWayServerChannelManager,
        ConverManager converManager, RouteManager routeManager, String notification) {
        this.gateWayServerChannelManager = gateWayServerChannelManager;
        this.converManager = converManager;
        this.routeManager = routeManager;
        this.notification = notification;
    }

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(notification, builder);
        NotifyMessage notifyMessage = builder.getNotifyMessage();

        List<String> uidList = converManager.getUidListByConver(notifyMessage.getTargetUid());
        for (String uid : uidList) {
            GatewayServerInfo server = routeManager.getServerByUid(uid);
            if (null != server) {
                Channel channel = gateWayServerChannelManager.getChannelByServer(server);
                if (channel != null) {
                    NotifyMessage downNotification = NotifyMessage.newBuilder().mergeFrom(notifyMessage)
                        .setTargetUid(uid).build();
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                        .setType(Type.NotifyMessage)
                        .setNotifyMessage(downNotification).build();
                    channel.writeAndFlush(ravenMessage);
                } else {
                    log.error("cannot find channel. server:{}", server);
                }
            } else {
                log.info("uid:{} no server to push down msg:{}.", uid, notifyMessage.getId());
            }
        }
    }
}
