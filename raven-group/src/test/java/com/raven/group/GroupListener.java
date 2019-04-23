package com.raven.group;

import com.raven.common.protos.Message.MessageAck;

public interface GroupListener {

    default void onMessageAckReceived(MessageAck ack) {
        System.out.println("onMessageAckReceived");
    }
}
