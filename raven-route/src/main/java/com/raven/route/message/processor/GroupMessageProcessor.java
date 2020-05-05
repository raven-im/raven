package com.raven.route.message.processor;

import com.raven.common.dubbo.AccessService;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
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
        UpDownMessage upDownMessage = builder.getUpDownMessage();

        //send to kafka
        // same conversation to same partition, keep the sequence in a conversation.
        kafka.send(Constants.KAFKA_TOPIC_GROUP_MSG, upDownMessage.getConvId(), message);

        //route to target access server.
        //TODO  处理群定向消息
        List<String> uidList = converManager.getUidListByConverExcludeSender(upDownMessage.getConvId(), upDownMessage.getFromUid());
        for (String uid : uidList) {
            UpDownMessage downMessage = UpDownMessage.newBuilder()
                    .mergeFrom(upDownMessage)
                    .build();
            RavenMessage ravenMessage = RavenMessage.newBuilder()
                    .setType(Type.UpDownMessage)
                    .setUpDownMessage(downMessage)
                    .build();
            String downMsg = JsonHelper.toJsonString(ravenMessage);
            // use uid for consistency hash, find a access server, and send it.
            accessService.outboundMsgSend(uid, downMsg);
        }
    }
}
