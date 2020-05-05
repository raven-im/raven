package com.raven.gateway.service;

import com.raven.common.dubbo.AccessService;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.SSMessage;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.JsonHelper;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.raven.common.utils.Constants.DEFAULT_SEPARATOR;

@Component("accessService")
@Slf4j
public class AccessServiceImpl implements AccessService {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Value("${netty.ip}")
    private String ip;

    @Value("${netty.tcp.port}")
    private String tcpPort;

    @Value("${netty.websocket.port}")
    private String wsPort;

    @Override
    public void outboundMsgSend(String uid, String msg) {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(msg, builder);
        SSMessage downMessage = builder.getSsMessage();

        log.info("receive down message:{}", JsonHelper.toJsonString(downMessage));
        String key = downMessage.getAppKey() + DEFAULT_SEPARATOR + uid;
        List<Channel> channels = uidChannelManager.getChannelsByUid(key);
        if (channels.isEmpty()) {
            //TODO  new router, maybe response with 304 to relocate the access server.   maybe re-create the hash ring.
            log.error("no channel match {}, maybe caused by bad routing.", key);
            return;
        }
        RavenMessage ravenMessage = buildRavenMessage(downMessage);
        for (Channel channel : channels) {
            channel.writeAndFlush(ravenMessage).addListener(future -> {
                if (!future.isSuccess()) {
                    log.error("push msg to uid:{} fail", key);
                    channel.close();
                }
            });
        }
    }

    @Override
    public String hashRouting(String uid) {
        String accessNode = ip + DEFAULT_SEPARATOR + tcpPort + DEFAULT_SEPARATOR + wsPort;
        log.info("uid[{}] access node: {}", uid, accessNode);
        return accessNode;
    }

    private RavenMessage buildRavenMessage(SSMessage ssMessage) {
        UpDownMessage message = UpDownMessage.newBuilder()
                .setId(ssMessage.getId())
                .setFromUid(ssMessage.getFromUid())
                .setConvId(ssMessage.getConvId())
                .setConverType(ssMessage.getConverType())
                .setContent(ssMessage.getContent())
                .build();
        return RavenMessage.newBuilder()
                .setType(RavenMessage.Type.UpDownMessage)
                .setUpDownMessage(message)
                .build();
    }
}
