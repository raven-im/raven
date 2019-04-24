package com.raven.single;

import com.raven.common.protos.Message.ConverAck;
import com.raven.common.protos.Message.MessageAck;

public interface MessageListener {

    default void onQueryAck(ConverAck ack) {
        System.out.println("onQueryAck");
    }
    default void onMessageAckReceived(MessageAck ack) {
        System.out.println("onMessageAckReceived");
    }

}
