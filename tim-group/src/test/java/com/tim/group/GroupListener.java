package com.tim.group;

import com.tim.common.protos.Group.GroupAck;
import com.tim.common.protos.Message.MessageAck;

public interface GroupListener {

    default void onGroupAckReceived(GroupAck ack) {
        System.out.println("onGroupAckReceived");
    }
    default void onMessageAckReceived(MessageAck ack) {
        System.out.println("onMessageAckReceived");
    }
}
