package com.tim.server.notify;

import com.tim.common.mqmsg.BaseMessage;
import com.tim.common.utils.Constants;
import com.tim.server.common.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MsgQueueConsumer {

    @Autowired
    private NotifySender notifySender;

    public void Consume(String message) {
        log.info("Consumer:{}", message);
    }
}
