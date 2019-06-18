package com.raven.group.tcp.manager;

import com.raven.common.loadbalance.AccessServerInfo;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import io.netty.channel.Channel;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GroupMessageProcessor implements Runnable {

    private ServerChannelManager internalServerChannelManager;

    private ConverManager converManager;

    private RouteManager routeManager;

    private UpDownMessage msg;

    public GroupMessageProcessor(
        ServerChannelManager internalServerChannelManager,
        ConverManager converManager, RouteManager routeManager,
        UpDownMessage msg) {
        this.internalServerChannelManager = internalServerChannelManager;
        this.converManager = converManager;
        this.routeManager = routeManager;
        this.msg = msg;
    }

    @Override
    public void run() {
        converManager.saveMsg2Conver(msg.getContent(), msg.getConverId());
        List<String> uidList = converManager.getUidListByConverExcludeSender(msg.getConverId(),
            msg.getFromUid());
        for (String uid : uidList) {
            AccessServerInfo server = routeManager.getServerByUid(uid);
            if (null != server) {
                Channel channel = internalServerChannelManager.getChannelByServer(server);
                if (channel != null) {
                    UpDownMessage downMessage = UpDownMessage.newBuilder()
                        .setId(msg.getId())
                        .setFromUid(msg.getFromUid())
                        .setTargetUid(uid)
                        .setConverType(msg.getConverType())
                        .setContent(msg.getContent())
                        .setConverId(msg.getConverId())
                        .build();
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                        .setType(Type.UpDownMessage)
                        .setUpDownMessage(downMessage).build();
                    channel.writeAndFlush(ravenMessage);
                } else {
                    log.error("cannot find channel. server:{}", server);
                    converManager.incrUserConverUnCount(uid, msg.getConverId(), 1);
                }
            } else {
                log.error("uid:{} no server to push down msg:{}.", uid, msg.getId());
                converManager.incrUserConverUnCount(uid, msg.getConverId(), 1);
            }
        }
    }
}
