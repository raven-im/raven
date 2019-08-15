package com.raven.gateway.serverapi.service.impl;

import com.raven.common.model.Conversation;
import com.raven.common.protos.Message.NotifyMessage;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.SnowFlake;
import com.raven.gateway.config.KafkaProducerManager;
import com.raven.gateway.serverapi.bean.param.ReqMsgParam;
import com.raven.gateway.serverapi.service.ServerApiService;
import com.raven.storage.conver.ConverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ServerApiServiceImpl implements ServerApiService {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private KafkaProducerManager kafkaProducerManager;

    @Override
    public Result notify2User(ReqMsgParam param) {
        return null;
    }

    @Override
    public Result notify2Conversation(ReqMsgParam param) {
        String convId = param.getTargetUid();
        String topic = Constants.KAFKA_TOPIC_NOTI_TO_CONV;
        if (StringUtils.isNotEmpty(convId)) {
            Conversation conv = converManager.getConversation(convId);

            if (conv == null) {
                log.error("illegal conversation id.");
                return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
            }
            long id = snowFlake.nextId();
            RavenMessage ravenMessage = buildRavenMessage(param, id);
            if (sendMsgToKafka(ravenMessage, id, topic)) {
                return Result.success(id);
            } else {
                log.error("send msg to kafka fail");
                return Result.failure(ResultCode.COMMON_KAFKA_PRODUCE_ERROR);
            }
        }
        return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
    }

    @Override
    public Result message2User(ReqMsgParam param) {
        return Result.failure(ResultCode.COMMON_NOT_IMPLEMENT);
    }

    @Override
    public Result message2Conversation(ReqMsgParam param) {
        return null;
    }

    private boolean sendMsgToKafka(RavenMessage ravenMessage, long id, String topic) {
        String message = JsonHelper.toJsonString(ravenMessage);
        if (StringUtils.isEmpty(message)) {
            return false;
        }
        log.info("protobuf to json message:{}", message);
        Result result = kafkaProducerManager.send(topic, String.valueOf(id), message);
        return result.getCode().intValue() == ResultCode.COMMON_SUCCESS.getCode();
    }

    private RavenMessage buildRavenMessage(ReqMsgParam param, long msgId) {

        NotifyMessage notifyMsg = NotifyMessage.newBuilder()
            .setId(msgId)
            .setTargetUid(param.getTargetUid())
            .setContent(param.getContent())
            .setType(Constants.SERVER_API_NOTIFY_NOTI)
            .setTime(System.currentTimeMillis())
            .build();
        return RavenMessage.newBuilder().setType(Type.NotifyMessage)
            .setNotifyMessage(notifyMsg).build();
    }
}