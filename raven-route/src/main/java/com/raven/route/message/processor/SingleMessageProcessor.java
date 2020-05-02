package com.raven.route.message.processor;

import com.raven.common.dubbo.MessageOutboundService;
import com.raven.common.protos.Message.RavenMessage;
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
public class SingleMessageProcessor implements Runnable {

    private ConverManager converManager;

    private String message;

    private KafkaProducerManager kafka;

    private MessageOutboundService msgOutService;

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(message, builder);
        UpDownMessage upDownMessage = builder.getUpDownMessage();

        //TODO  发单聊的时候，  API 只能发到 二人中的一个， 一般是conversation建立起来的时候的targetUid.
        //send to kafka
        kafka.send(Constants.KAFKA_TOPIC_SINGLE_MSG, upDownMessage.getConverId(), message);

        //route to target access server.
        List<String> uidList = converManager.getUidListByConverExcludeSender(upDownMessage.getConverId(),
                upDownMessage.getFromUid());
        for (String uid : uidList) {
            // use uid for consistency hash, find a access server, and send it.
            // TODO merge same message (uids to same access)
            msgOutService.outboundMsgSend(message);
        }

    }
}
