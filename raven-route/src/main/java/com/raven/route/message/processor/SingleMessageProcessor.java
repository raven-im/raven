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
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SingleMessageProcessor implements Runnable {

    private ServerChannelManager gateWayServerChannelManager;

    private ConverManager converManager;

    private RouteManager routeManager;

    private String message;

    public SingleMessageProcessor(ServerChannelManager gateWayServerChannelManager,
                                  ConverManager converManager, RouteManager routeManager, String message) {
        this.gateWayServerChannelManager = gateWayServerChannelManager;
        this.converManager = converManager;
        this.routeManager = routeManager;
        this.message = message;
    }

    @Override
    public void run() {
        //TODO  发单聊的时候，  API 只能发到 二人中的一个， 一般是conversation建立起来的时候的targetUid.
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(message, builder);
        UpDownMessage upDownMessage = builder.getUpDownMessage();
        converManager.saveMsg2Conver(upDownMessage.getContent(), upDownMessage.getConverId());
        List<String> uidList = converManager
                .getUidListByConverExcludeSender(upDownMessage.getConverId(),
                        upDownMessage.getFromUid());
        for (String uid : uidList) {
            GatewayServerInfo server = routeManager.getServerByUid(uid);
            if (null != server) {
                UpDownMessage downMessage = UpDownMessage.newBuilder().mergeFrom(upDownMessage).build();
                Channel channel = gateWayServerChannelManager.getChannelByServer(server);
                if (channel != null) {
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                            .setType(Type.UpDownMessage)
                            .setUpDownMessage(downMessage).build();
                    channel.writeAndFlush(ravenMessage);
                } else {
                    log.error("cannot find channel！ server:{}", server);
                }
            } else {
                log.info("uid:{} no server to push down msg:{}.", uid, upDownMessage.getId());
            }
        }
    }
}
