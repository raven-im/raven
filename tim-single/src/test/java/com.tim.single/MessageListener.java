package com.tim.single;

import com.tim.common.protos.Conversation.ConversationAck;
import com.tim.common.protos.Message.MessageAck;

public interface MessageListener {

    default void onQueryAck(ConversationAck ack) {
        System.out.println("onQueryAck");
    }
    default void onMessageAckReceived(MessageAck ack) {
        System.out.println("onMessageAckReceived");
    }

}
