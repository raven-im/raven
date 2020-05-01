package com.raven.admin.notification.service;

import com.raven.admin.messages.bean.param.ReqMsgParam;
import com.raven.common.result.Result;

public interface NotificationService {
    Result notify2Conversation(ReqMsgParam param);
}
