package com.tim.single;

import com.tim.common.protos.Message.MessageAck;

public interface MessageListener {
    void onMessageAckReceived(MessageAck ack);
}
