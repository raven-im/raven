package com.raven.route.message.listenser;

import com.raven.common.kafka.MessageListener;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import com.raven.route.config.CustomConfig;
import com.raven.route.message.processor.GroupMessageProcessor;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupMessageListener extends MessageListener<String, String> {

    @Autowired
    private ServerChannelManager gateWayServerChannelManager;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private RouteManager routeManager;

    public GroupMessageListener() {
        this.setTopic(Constants.KAFKA_TOPIC_GROUP_MSG);
    }

    @Override
    public void receive(String topic, String key, String message) {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(message, builder);
        UpDownMessage upDownMessage = builder.getUpDownMessage();
        converManager.saveMsg2Conver(upDownMessage.getContent(), upDownMessage.getConverId());
        GroupMessageProcessor processor = new GroupMessageProcessor(gateWayServerChannelManager,
            converManager, routeManager, upDownMessage);
        CustomConfig.msgProcessorSevice.submit(processor);
    }
}
