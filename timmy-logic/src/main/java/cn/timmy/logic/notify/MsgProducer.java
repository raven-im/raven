package cn.timmy.logic.notify;

import cn.timmy.common.mqmsg.BaseMessage;
import cn.timmy.common.mqmsg.FriendNotifyMsg;
import cn.timmy.common.mqmsg.FriendNotifyMsg.FriendNotifyType;
import cn.timmy.common.mqmsg.UserNotifyMsg;
import cn.timmy.common.mqmsg.UserNotifyMsg.UserNotifyType;
import cn.timmy.common.utils.Constants;
import cn.timmy.common.utils.DateTimeUtils;
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
        sender.send2Queue(Constants.MSG_QUEUE_NAME, baseMessage);
    }

    public void newFriendRequest(String fromUid, String toUid, Integer requestId) {
        FriendNotifyMsg notifyMsg = new FriendNotifyMsg(FriendNotifyType.REQUEST.getValue(),
            requestId);
        BaseMessage baseMessage = new BaseMessage(fromUid, toUid);
        baseMessage.setType(Constants.NOTIFY_TYPE_FRIEND);
        baseMessage.setContent(notifyMsg.toString());
        baseMessage.setSend_time(DateTimeUtils.currentUTC().getTime());
        sender.send2Queue(Constants.MSG_QUEUE_NAME, baseMessage);
    }

    public void newRequestReply(String fromUid, String toUid, Integer requestId) {
        FriendNotifyMsg notifyMsg = new FriendNotifyMsg(FriendNotifyType.REPLY.getValue(),
            requestId);
        BaseMessage baseMessage = new BaseMessage(fromUid, toUid);
        baseMessage.setType(Constants.NOTIFY_TYPE_FRIEND);
        baseMessage.setContent(notifyMsg.toString());
        baseMessage.setSend_time(DateTimeUtils.currentUTC().getTime());
        sender.send2Queue(Constants.MSG_QUEUE_NAME, baseMessage);
    }

    public void friendRequestAccept(String fromUid, String toUid, Integer requestId) {
        FriendNotifyMsg notifyMsg = new FriendNotifyMsg(FriendNotifyType.ACCEPT.getValue(),
            requestId);
        BaseMessage baseMessage = new BaseMessage(fromUid, toUid);
        baseMessage.setType(Constants.NOTIFY_TYPE_FRIEND);
        baseMessage.setContent(notifyMsg.toString());
        baseMessage.setSend_time(DateTimeUtils.currentUTC().getTime());
        sender.send2Queue(Constants.MSG_QUEUE_NAME, baseMessage);
    }

}
