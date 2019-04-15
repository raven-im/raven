package com.tim.group;

import com.tim.common.protos.Message.MessageAck;

public interface GroupListener {

    default void onMessageAckReceived(MessageAck ack) {
        System.out.println("onMessageAckReceived");
    }
}
