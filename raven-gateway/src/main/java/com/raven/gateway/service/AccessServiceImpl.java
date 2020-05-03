package com.raven.gateway.service;

import com.raven.common.dubbo.AccessService;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.RavenMessage;
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
        UpDownMessage downMessage = builder.getUpDownMessage();

        log.info("receive down message:{}", JsonHelper.toJsonString(downMessage));
        List<Channel> channels = uidChannelManager.getChannelsById(downMessage.getTargetUid());
        if (channels.isEmpty()) {
            //TODO  new router, maybe response with 304 to relocate the access server.   maybe re-create the hash ring.
            log.error("no channel match {}, maybe caused by bad routing.", downMessage.getTargetUid());
            return;
        }
        RavenMessage ravenMessage = RavenMessage.newBuilder()
                .setType(RavenMessage.Type.UpDownMessage)
                .setUpDownMessage(downMessage)
                .build();
        for (Channel channel : channels) {
            channel.writeAndFlush(ravenMessage).addListener(future -> {
                if (!future.isSuccess()) {
                    log.error("push msg to uid:{} fail", downMessage.getTargetUid());
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
}
