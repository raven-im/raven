package com.raven.admin.notification.service.impl;

import com.raven.admin.messages.bean.param.ReqMsgParam;
import com.raven.admin.notification.service.NotificationService;
import com.raven.common.dubbo.MessageService;
import com.raven.common.model.Conversation;
import com.raven.common.protos.Message.*;
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
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private MessageService msgService;

    @Override
    public Result notify2Conversation(ReqMsgParam param) {
        String convId = param.getTargetUid();
        if (StringUtils.isNotEmpty(convId)) {
            Conversation conv = converManager.getConversation(convId);

            if (conv == null) {
                log.error("illegal conversation id.");
                return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
            }
            long id = snowFlake.nextId();
            RavenMessage ravenMessage = buildNotification(param, id, conv);
            if (sendNotification(ravenMessage, conv.getType(), id)) {
                return Result.success(id);
            } else {
                log.error("API send notification to conversation fail");
                return Result.failure(ResultCode.COMMON_SERVER_ERROR);
            }
        }
        return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
    }

    private boolean sendNotification(RavenMessage ravenMessage, int type, long id) {
        log.info("send Msg to {} : {}", id, type);
        String message = JsonHelper.toJsonString(ravenMessage);
        if (StringUtils.isEmpty(message)) {
            return false;
        }
        log.debug("protobuf to json message:{}", message);
        if (type == 0) {
            msgService.singleMsgSend(message);
        } else if (type == 1) {
            msgService.groupMsgSend(message);
        } else {
            log.error("type error, message not sent:{}", message);
            return false;
        }
        return true;
    }

    private RavenMessage buildNotification(ReqMsgParam param, long msgId, Conversation conv) {
        MessageContent content = MessageContent.newBuilder()
                .setType(MessageType.TEXT) //TODO still build message. but Message Type different. MessageType.NOTIFY ????
                .setContent(param.getContent()) // MessageType.NOTIFY ,content different.
                .setTime(System.currentTimeMillis())
                .build();
        UpDownMessage upMessage = UpDownMessage.newBuilder().setId(msgId)
//            .setCid()
                .setFromUid(param.getFromUid())
                .setTargetUid(param.getTargetUid())
                .setContent(content)
                .setConverType(conv.getType() == 1 ? ConverType.GROUP : ConverType.SINGLE)
//            .setGroupId(upDownMessage.getGroupId())
                .build();
        return RavenMessage.newBuilder().setType(RavenMessage.Type.UpDownMessage)
                .setUpDownMessage(upMessage).build();
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
}
