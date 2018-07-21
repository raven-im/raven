package cn.timmy.message.common;

import cn.timmy.common.mqmsg.BaseMessage;
import cn.timmy.common.utils.GsonHelper;

public abstract class BaseConsumer {

    protected BaseMessage convert2MsgObject(String message) {
        BaseMessage msgObject = GsonHelper.getGson().fromJson(message, BaseMessage.class);
        return msgObject;
    }

}
