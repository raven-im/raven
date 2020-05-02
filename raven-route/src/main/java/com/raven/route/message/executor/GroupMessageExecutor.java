package com.raven.route.message.executor;

import com.raven.common.dubbo.MessageOutboundService;
import com.raven.route.config.CustomConfig;
import com.raven.route.config.KafkaProducerManager;
import com.raven.route.message.processor.GroupMessageProcessor;
import com.raven.storage.conver.ConverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupMessageExecutor {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private KafkaProducerManager kafka;

    @Autowired
    private MessageOutboundService msgOutService;

    public void saveAndSendMsg(String message) {
        GroupMessageProcessor processor = new GroupMessageProcessor(
                converManager, message, kafka, msgOutService);
        CustomConfig.msgProcessorService.submit(processor);
    }
}
