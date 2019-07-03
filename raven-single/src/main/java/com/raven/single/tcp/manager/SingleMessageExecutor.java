package com.raven.single.tcp.manager;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
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
public class SingleMessageExecutor {

    @Autowired
    private ServerChannelManager internalServerChannelManager;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private RouteManager routeManager;

    private ExecutorService executorService = Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    public void saveAndSendMsg(String message) {
        SingleMessageProcessor processor = new SingleMessageProcessor(internalServerChannelManager,
            converManager, routeManager, message);
        executorService.submit(processor);
    }

}
