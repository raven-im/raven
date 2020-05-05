package com.raven.admin.messages.service.impl;

import com.raven.admin.messages.bean.param.ReqMsgParam;
import com.raven.admin.messages.service.MessagesService;
import com.raven.common.dubbo.MessageService;
import com.raven.common.enums.MessageType;
import com.raven.common.model.Conversation;
import com.raven.common.protos.Message.*;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.SnowFlake;
import com.raven.storage.conver.ConverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageServiceImpl implements MessagesService {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private MessageService msgService;

    @Override
    public Result message2User(ReqMsgParam param) {
        return Result.failure(ResultCode.COMMON_NOT_IMPLEMENT);
    }

    @Override
    public Result message2Conversation(ReqMsgParam param) {
        String convId = param.getTargetUid();
        ConverType type = ConverType.GROUP;
        if (StringUtils.isNotEmpty(convId)) {
            Conversation conv = converManager.getConversation(convId);

            if (conv == null) {
                log.error("illegal conversation id.");
                return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
            }
            if (conv.getType() == ConverType.SINGLE.getNumber()) {
                type = ConverType.SINGLE;
            }
            long id = snowFlake.nextId();
            RavenMessage ravenMessage = buildMessage(param, id, type);
            if (sendMsg(ravenMessage, type, id)) {
                return Result.success(id);
            } else {
                log.error("API send msg fail");
                return Result.failure(ResultCode.COMMON_SERVER_ERROR);
            }
        }
        return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
    }

    private boolean sendMsg(RavenMessage ravenMessage, ConverType type, long id) {
        log.info("send Msg to {} : {}", id, type.getNumber());
        String message = JsonHelper.toJsonString(ravenMessage);
        if (StringUtils.isEmpty(message)) {
            return false;
        }
        log.debug("protobuf to json message:{}", message);
        if (type == ConverType.SINGLE) {
            msgService.singleMsgSend(message);
        } else if (type == ConverType.GROUP) {
            msgService.groupMsgSend(message);
        } else {
            log.error("type error, message not sent:{}", message);
            return false;
        }
        return true;
    }

    private RavenMessage buildMessage(ReqMsgParam param, long msgId, ConverType type) {
        MessageContent content = MessageContent.newBuilder()
                .setType(MessageType.TEXT.getType()) //TODO  server api only support text message now.
                .setContent(param.getContent())
                .setTime(System.currentTimeMillis()).build();
        UpDownMessage upMessage = UpDownMessage.newBuilder()
                .setId(msgId)
                .setFromUid(param.getFromUid())
                .setConvId(param.getTargetUid())
                .setContent(content)
                .setConverType(type)
                .build();
        return RavenMessage.newBuilder().setType(Type.UpDownMessage)
                .setUpDownMessage(upMessage).build();
    }
}
