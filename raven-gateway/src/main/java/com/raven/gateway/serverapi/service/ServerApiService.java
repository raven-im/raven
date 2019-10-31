package com.raven.gateway.serverapi.service;

import com.raven.common.result.Result;
import com.raven.gateway.serverapi.bean.param.ReqMsgParam;

public interface ServerApiService {
    Result notify2User(ReqMsgParam param);
    Result notify2Conversation(ReqMsgParam param);
    Result message2User(ReqMsgParam param);
    Result message2Conversation(ReqMsgParam param);
}
