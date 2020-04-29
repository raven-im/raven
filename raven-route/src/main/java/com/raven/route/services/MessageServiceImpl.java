package com.raven.route.services;

import com.raven.common.dubbo.MessageService;
import com.raven.route.message.executor.GroupMessageExecutor;
import com.raven.route.message.executor.NotifyConvExecutor;
import com.raven.route.message.executor.NotifyUserExecutor;
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

    @Autowired
    private NotifyConvExecutor conNotifyExecutor;

    @Autowired
    private NotifyUserExecutor userNotifyExecutor;

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

    @Override
    public void converNotify(String notify) {
        log.info(" conversation notify sent.");
        conNotifyExecutor.sendNotification(notify);
    }

    @Override
    public void userNotify(String notify) {
        log.info(" user notification sent.");
        userNotifyExecutor.sendNotification(notify);
    }
}
