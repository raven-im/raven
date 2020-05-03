package com.raven.route.message.executor;

import com.raven.common.dubbo.AccessService;
import com.raven.route.config.CustomConfig;
import com.raven.route.config.KafkaProducerManager;
import com.raven.route.message.processor.SingleMessageProcessor;
import com.raven.storage.conver.ConverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SingleMessageExecutor {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private KafkaProducerManager kafka;

    @Autowired
    private AccessService accessService;

    public void saveAndSendMsg(String message) {
        SingleMessageProcessor processor = new SingleMessageProcessor(converManager, message, kafka, accessService);
        CustomConfig.msgProcessorService.submit(processor);
    }

}
