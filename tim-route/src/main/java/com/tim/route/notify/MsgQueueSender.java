package com.tim.route.notify;

import com.tim.common.utils.Constants;
import com.tim.common.utils.GsonHelper;
import com.tim.common.utils.UidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author zxx Description  消息队列发送者 Date Created on 2018/6/30
 */
@Component
@Slf4j
public class MsgQueueSender {

    public void send2Queue(String routingKey, Object message) {
        log.info("send message:{} to queue:{}", message, routingKey);
    }

}
