package com.raven.route.message.processor;

import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.JsonHelper;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import io.netty.channel.Channel;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupMessageProcessor implements Runnable {

    private ServerChannelManager gateWayServerChannelManager;

    private ConverManager converManager;

    private RouteManager routeManager;

    private UpDownMessage upDownMessage;

    public GroupMessageProcessor(ServerChannelManager gateWayServerChannelManager,
        ConverManager converManager, RouteManager routeManager, UpDownMessage upDownMessage) {
        this.gateWayServerChannelManager = gateWayServerChannelManager;
        this.converManager = converManager;
        this.routeManager = routeManager;
        this.upDownMessage = upDownMessage;
    }

    @Override
    public void run() {
        List<String> uidList = converManager
            .getUidListByConverExcludeSender(upDownMessage.getConverId(),
                upDownMessage.getFromUid());
        for (String uid : uidList) {
            GatewayServerInfo server = routeManager.getServerByUid(uid);
            if (null != server) {
                Channel channel = gateWayServerChannelManager.getChannelByServer(server);
                if (channel != null) {
                    UpDownMessage downMessage = UpDownMessage.newBuilder().mergeFrom(upDownMessage)
                        .setTargetUid(uid).build();
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                        .setType(Type.UpDownMessage)
                        .setUpDownMessage(downMessage).build();
                    channel.writeAndFlush(ravenMessage);
                } else {
                    log.error("cannot find channel. server:{}", server);
                }
            } else {
                log.info("uid:{} no server to push down msg:{}.", uid, upDownMessage.getId());
            }
        }
    }
}
