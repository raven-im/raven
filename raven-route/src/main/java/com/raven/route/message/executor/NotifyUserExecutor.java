package com.raven.route.message.executor;

import com.raven.common.netty.ServerChannelManager;
import com.raven.route.config.CustomConfig;
import com.raven.route.message.processor.NotifyUserProcessor;
import com.raven.storage.route.RouteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotifyUserExecutor {

    @Autowired
    private ServerChannelManager gateWayServerChannelManager;

    @Autowired
    private RouteManager routeManager;

    public void sendNotification(String notification) {
        NotifyUserProcessor processor = new NotifyUserProcessor(gateWayServerChannelManager,
            routeManager, notification);
        CustomConfig.msgProcessorSevice.submit(processor);
    }
}
