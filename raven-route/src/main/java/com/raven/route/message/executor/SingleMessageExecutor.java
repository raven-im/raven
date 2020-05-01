package com.raven.route.message.executor;

import com.raven.common.netty.ServerChannelManager;
import com.raven.route.config.CustomConfig;
import com.raven.route.config.KafkaProducerManager;
import com.raven.route.message.processor.SingleMessageProcessor;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SingleMessageExecutor {

    @Autowired
    private ServerChannelManager gateWayServerChannelManager;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private RouteManager routeManager;

    @Autowired
    private KafkaProducerManager kafka;

    public void saveAndSendMsg(String message) {
        SingleMessageProcessor processor = new SingleMessageProcessor(gateWayServerChannelManager,
                converManager, routeManager, message, kafka);
        CustomConfig.msgProcessorSevice.submit(processor);
    }

}
