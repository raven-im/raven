package com.raven.admin.notification.service.impl;

import com.raven.admin.messages.bean.param.ReqMsgParam;
import com.raven.admin.notification.service.NotificationService;
import com.raven.common.dubbo.MessageService;
import com.raven.common.model.Conversation;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.NotifyMessage;
import com.raven.common.protos.Message.NotifyType;
import com.raven.common.protos.Message.RavenMessage;
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
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private ConverManager converManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private MessageService msgService;

    @Override
    public Result notify2User(ReqMsgParam param) {
        String userId = param.getTargetUid();
        if (StringUtils.isNotEmpty(userId)) {
            long id = snowFlake.nextId();
            RavenMessage ravenMessage = buildNotification2User(param, NotifyType.USER, id);
            if (sendNotification(ravenMessage, NotifyType.USER, id)) {
                return Result.success(id);
            } else {
                log.error("API send notification to user fail");
                return Result.failure(ResultCode.COMMON_SERVER_ERROR);
            }
        }
        return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
    }

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
            RavenMessage ravenMessage = buildNotification2Conv(param, NotifyType.CONVERSATION, id, conv);
            if (sendNotification(ravenMessage, NotifyType.CONVERSATION, id)) {
                return Result.success(id);
            } else {
                log.error("API send notification to conversation fail");
                return Result.failure(ResultCode.COMMON_SERVER_ERROR);
            }
        }
        return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
    }

    private boolean sendNotification(RavenMessage ravenMessage, NotifyType type, long id) {
        log.info("Notify to {} : {}", id, type.getNumber());
        String notify = JsonHelper.toJsonString(ravenMessage);
        if (StringUtils.isEmpty(notify)) {
            return false;
        }
        log.debug("protobuf to json notify:{}", notify);
        if (type == NotifyType.CONVERSATION) {
            msgService.converNotify(notify);
        } else if (type == NotifyType.USER) {
            msgService.userNotify(notify);
        } else {
            log.error("type not support.");
            return false;
        }

        return true;
    }

    private RavenMessage buildNotification2User(ReqMsgParam param, NotifyType type, long msgId) {
        NotifyMessage notifyMsg = NotifyMessage.newBuilder()
                .setId(msgId)
                .setTargetUid(param.getTargetUid())
                .setContent(param.getContent())
                .setType(type)
                .setFromUid(param.getFromUid())
                .setTime(System.currentTimeMillis())
                .build();
        return RavenMessage.newBuilder().setType(Type.NotifyMessage)
                .setNotifyMessage(notifyMsg).build();
    }

    private RavenMessage buildNotification2Conv(ReqMsgParam param, NotifyType type, long msgId, Conversation conv) {
        NotifyMessage notifyMsg = NotifyMessage.newBuilder()
                .setId(msgId)
                .setTargetUid(param.getTargetUid())
                .setContent(param.getContent())
                .setType(type)
                .setConvType(conv.getType() == ConverType.GROUP.getNumber() ? ConverType.GROUP : ConverType.SINGLE)
                .setConverId(conv.getId())
                .setFromUid(param.getFromUid())
                .setTime(System.currentTimeMillis())
                .build();
        return RavenMessage.newBuilder().setType(Type.NotifyMessage)
                .setNotifyMessage(notifyMsg).build();
    }
}
