package com.tim.server.common;

import com.tim.common.mqmsg.BaseMessage;
import com.tim.common.utils.GsonHelper;

public abstract class BaseConsumer {

    protected BaseMessage convert2MsgObject(String message) {
        BaseMessage msgObject = GsonHelper.getGson().fromJson(message, BaseMessage.class);
        return msgObject;
    }

}
