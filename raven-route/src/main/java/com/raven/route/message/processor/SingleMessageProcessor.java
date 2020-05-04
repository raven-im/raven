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
public class SingleMessageProcessor implements Runnable {

    private ConverManager converManager;

    private String message;

    private KafkaProducerManager kafka;

    private AccessService accessService;

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(message, builder);
        SSMessage ssMessage = builder.getSsMessage();

        //TODO  发单聊的时候，  API 只能发到 二人中的一个， 一般是conversation建立起来的时候的targetUid.
        //send to kafka
        //以targetUid为序，保证同一个会话有序
        kafka.send(Constants.KAFKA_TOPIC_SINGLE_MSG, ssMessage.getConvId(), message);

        //route to target access server.
        List<String> uidList = converManager.getUidListByConverExcludeSender(ssMessage.getConvId(),
                ssMessage.getFromUid());
        for (String uid : uidList) {
            // use uid for consistency hash, find a access server, and send it.
            accessService.outboundMsgSend(uid, message);
        }

    }
}
