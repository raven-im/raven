package com.raven.admin.messages.service;

import com.raven.admin.messages.bean.param.ReqMsgParam;
import com.raven.common.result.Result;

public interface MessagesService {
    Result message2User(ReqMsgParam param);
    Result message2Conversation(ReqMsgParam param);
}
