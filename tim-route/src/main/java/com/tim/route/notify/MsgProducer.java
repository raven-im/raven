package com.tim.route.notify;

import com.tim.common.mqmsg.BaseMessage;
import com.tim.common.mqmsg.FriendNotifyMsg;
import com.tim.common.mqmsg.FriendNotifyMsg.FriendNotifyType;
import com.tim.common.mqmsg.UserNotifyMsg;
import com.tim.common.mqmsg.UserNotifyMsg.UserNotifyType;
import com.tim.common.utils.Constants;
import com.tim.common.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 消息生产者
 * Date Created on 2018/7/1
 */
@Component
public class MsgProducer {

    @Autowired
    private MsgQueueSender sender;

    public void pwdChanged(String fromUid, String toUid) {
        UserNotifyMsg notifyMsg = new UserNotifyMsg(UserNotifyType.PASSWORD_CHANGE.getValue());
        BaseMessage baseMessage = new BaseMessage(fromUid, toUid);
        baseMessage.setType(Constants.NOTIFY_TYPE_USER);
        baseMessage.setContent(notifyMsg.toString());
        baseMessage.setSend_time(DateTimeUtils.currentUTC().getTime());
        sender.send2Queue(Constants.RABBIT_ROUTKEY_NOTIFY_LOGIC, baseMessage);
    }

    public void newFriendRequest(String fromUid, String toUid, Integer requestId) {
        FriendNotifyMsg notifyMsg = new FriendNotifyMsg(FriendNotifyType.REQUEST.getValue(),
            requestId);
        BaseMessage baseMessage = new BaseMessage(fromUid, toUid);
        baseMessage.setType(Constants.NOTIFY_TYPE_FRIEND);
        baseMessage.setContent(notifyMsg.toString());
        baseMessage.setSend_time(DateTimeUtils.currentUTC().getTime());
        sender.send2Queue(Constants.RABBIT_ROUTKEY_NOTIFY_LOGIC, baseMessage);
    }

    public void newRequestReply(String fromUid, String toUid, Integer requestId) {
        FriendNotifyMsg notifyMsg = new FriendNotifyMsg(FriendNotifyType.REPLY.getValue(),
            requestId);
        BaseMessage baseMessage = new BaseMessage(fromUid, toUid);
        baseMessage.setType(Constants.NOTIFY_TYPE_FRIEND);
        baseMessage.setContent(notifyMsg.toString());
        baseMessage.setSend_time(DateTimeUtils.currentUTC().getTime());
        sender.send2Queue(Constants.RABBIT_ROUTKEY_NOTIFY_LOGIC, baseMessage);
    }

    public void friendRequestAccept(String fromUid, String toUid, Integer requestId) {
        FriendNotifyMsg notifyMsg = new FriendNotifyMsg(FriendNotifyType.ACCEPT.getValue(),
            requestId);
        BaseMessage baseMessage = new BaseMessage(fromUid, toUid);
        baseMessage.setType(Constants.NOTIFY_TYPE_FRIEND);
        baseMessage.setContent(notifyMsg.toString());
        baseMessage.setSend_time(DateTimeUtils.currentUTC().getTime());
        sender.send2Queue(Constants.RABBIT_ROUTKEY_NOTIFY_LOGIC, baseMessage);
    }

}
