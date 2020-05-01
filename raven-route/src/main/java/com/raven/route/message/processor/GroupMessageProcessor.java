package com.raven.route.message.processor;

import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import com.raven.route.config.KafkaProducerManager;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class GroupMessageProcessor implements Runnable {

    private ServerChannelManager gateWayServerChannelManager;

    private ConverManager converManager;

    private RouteManager routeManager;

    private String message;

    private KafkaProducerManager kafka;

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(message, builder);
        UpDownMessage upDownMessage = builder.getUpDownMessage();

        //send to kafka
        // same conversation to same partition, keep the sequence in a conversation.
        kafka.send(Constants.KAFKA_TOPIC_GROUP_MSG, upDownMessage.getConverId(), message);

        //route to target access server.
        List<String> uidList = converManager.getUidListByConverExcludeSender(upDownMessage.getConverId(), upDownMessage.getFromUid());
        for (String uid : uidList) {
            GatewayServerInfo server = routeManager.getServerByUid(uid);
            if (null != server) {
                Channel channel = gateWayServerChannelManager.getChannelByServer(server);
                if (channel != null) {
                    UpDownMessage downMessage = UpDownMessage.newBuilder()
                            .mergeFrom(upDownMessage)
                            .setTargetUid(uid)
                            .build();
                    RavenMessage ravenMessage = RavenMessage.newBuilder()
                            .setType(Type.UpDownMessage)
                            .setUpDownMessage(downMessage)
                            .build();
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
