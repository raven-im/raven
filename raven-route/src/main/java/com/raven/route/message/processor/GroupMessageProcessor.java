package com.raven.route.message.processor;

import com.raven.common.dubbo.AccessService;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.SSMessage;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import com.raven.route.config.KafkaProducerManager;
import com.raven.storage.conver.ConverManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class GroupMessageProcessor implements Runnable {

    private ConverManager converManager;

    private String message;

    private KafkaProducerManager kafka;

    private AccessService accessService;

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(message, builder);
        SSMessage ssMessage = builder.getSsMessage();

        //send to kafka
        // same conversation to same partition, keep the sequence in a conversation.
        kafka.send(Constants.KAFKA_TOPIC_GROUP_MSG, ssMessage.getConvId(), message);

        //route to target access server.
        //TODO  处理群定向消息
        List<String> uidList = converManager.getUidListByConverExcludeSender(ssMessage.getConvId(),
                ssMessage.getFromUid());
        for (String uid : uidList) {
            // use uid for consistency hash, find a access server, and send it.
            accessService.outboundMsgSend(uid, message);
        }
    }
}
