package com.raven.route.services;

import com.raven.common.dubbo.MessageService;
import com.raven.route.message.executor.GroupMessageExecutor;
import com.raven.route.message.executor.SingleMessageExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("msgService")
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private SingleMessageExecutor singleMessageExecutor;

    @Autowired
    private GroupMessageExecutor groupMessageExecutor;

    @Override
    public void singleMsgSend(String msg) {
        log.info(" single msg sent.");
        singleMessageExecutor.saveAndSendMsg(msg);
    }

    @Override
    public void groupMsgSend(String msg) {
        log.info(" group msg sent.");
        groupMessageExecutor.saveAndSendMsg(msg);
    }

}
