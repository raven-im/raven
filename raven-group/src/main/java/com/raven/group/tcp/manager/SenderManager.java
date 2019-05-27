package com.raven.group.tcp.manager;

import com.raven.common.loadbalance.Server;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import io.netty.channel.Channel;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SenderManager {

    @Autowired
    private ServerChannelManager internalServerChannelManager;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private RouteManager routeManager;

    public void sendMessage(UpDownMessage msg) {
        List<String> uidList = converManager.getUidListByConverExcludeSender(msg.getConverId(),
            msg.getFromUid());
        for (String uid : uidList) {
            Server server = routeManager.getInternalServerByUid(uid);
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
                    log.info("downstream msg {} sent.", downMessage.getId());
                } else {
                    log.error("cannot find channel. server:{}", server);
                }
            } else {
                log.info("downstream push msg {} sent.", msg.getId());
                converManager.incrUserConverUnCount(uid, msg.getConverId(), 1);
            }
        }

    }
}
