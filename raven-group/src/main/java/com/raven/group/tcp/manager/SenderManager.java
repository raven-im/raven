package com.raven.group.tcp.manager;

import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private ExecutorService executorService = Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    public void sendMessage(UpDownMessage msg) {
        GroupMessageProcessor processor = new GroupMessageProcessor(internalServerChannelManager,
            converManager, routeManager, msg);
        executorService.submit(processor);
    }
}
