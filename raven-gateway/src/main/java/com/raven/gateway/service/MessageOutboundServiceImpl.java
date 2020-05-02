package com.raven.gateway.service;

import com.raven.common.dubbo.MessageOutboundService;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.*;
import com.raven.common.utils.JsonHelper;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("outService")
@Slf4j
public class MessageOutboundServiceImpl implements MessageOutboundService {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Override
    public void outboundMsgSend(String msg) {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(msg, builder);
        UpDownMessage downMessage = builder.getUpDownMessage();

        log.debug("receive down message:{}", JsonHelper.toJsonString(downMessage));
        List<Channel> channels = uidChannelManager.getChannelsById(downMessage.getTargetUid());
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
}
