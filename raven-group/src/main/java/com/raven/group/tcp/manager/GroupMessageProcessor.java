package com.raven.group.tcp.manager;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
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

    private String message;

    public GroupMessageProcessor(ServerChannelManager internalServerChannelManager,
        ConverManager converManager, RouteManager routeManager, String message) {
        this.internalServerChannelManager = internalServerChannelManager;
        this.converManager = converManager;
        this.routeManager = routeManager;
        this.message = message;
    }

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        try {
            JsonFormat.merge(message, builder);
        } catch (ParseException e) {
            log.error("parse message error", e);
        }
        UpDownMessage upDownMessage = builder.getUpDownMessage();
        converManager.saveMsg2Conver(upDownMessage.getContent(), upDownMessage.getConverId());
        List<String> uidList = converManager
            .getUidListByConverExcludeSender(upDownMessage.getConverId(),
                upDownMessage.getFromUid());
        for (String uid : uidList) {
            AccessServerInfo server = routeManager.getServerByUid(uid);
            if (null != server) {
                Channel channel = internalServerChannelManager.getChannelByServer(server);
                if (channel != null) {
                    UpDownMessage downMessage = UpDownMessage.newBuilder()
                        .setId(upDownMessage.getId())
                        .setFromUid(upDownMessage.getFromUid())
                        .setTargetUid(uid)
                        .setConverType(upDownMessage.getConverType())
                        .setContent(upDownMessage.getContent())
                        .setConverId(upDownMessage.getConverId())
                        .build();
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                        .setType(Type.UpDownMessage)
                        .setUpDownMessage(downMessage).build();
                    channel.writeAndFlush(ravenMessage);
                } else {
                    log.error("cannot find channel. server:{}", server);
                    converManager.incrUserConverUnCount(uid, upDownMessage.getConverId(), 1);
                }
            } else {
                log.error("uid:{} no server to push down msg:{}.", uid, upDownMessage.getId());
                converManager.incrUserConverUnCount(uid, upDownMessage.getConverId(), 1);
            }
        }
    }
}
