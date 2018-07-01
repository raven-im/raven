package cn.timmy.message.common;

import cn.timmy.common.mqmsg.BaseMessage;
import cn.timmy.common.utils.GsonHelper;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/7/1
 */
public abstract class BaseConsumer {

    protected BaseMessage convert2MsgObject(String message) {
        BaseMessage msgObject = GsonHelper.getGson().fromJson(message, BaseMessage.class);
        return msgObject;
    }

}
