package com.raven.single.tcp.manager;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SingleMessageProcessor implements Runnable {

    private ServerChannelManager internalServerChannelManager;

    private ConverManager converManager;

    private RouteManager routeManager;

    private UpDownMessage msg;

    public SingleMessageProcessor(
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
        List<String> uidList = converManager.getUidListByConverExcludeSender(msg.getConverId(),
            msg.getFromUid());
        for (String uid : uidList) {
            UpDownMessage downMessage = msg.toBuilder().setTargetUid(uid).build();
            AccessServerInfo server = routeManager.getServerByUid(uid);
            if (null != server) {
                Channel channel = internalServerChannelManager.getChannelByServer(server);
                if (channel != null) {
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                        .setType(Type.UpDownMessage)
                        .setUpDownMessage(downMessage).build();
                    channel.writeAndFlush(ravenMessage);
                    log.info("send down msg {}", downMessage);
                } else {
                    log.error("cannot find channel. server:{}", server);
                }
            } else {
                converManager.incrUserConverUnCount(uid, msg.getConverId(), 1);
            }
        }
    }
}
