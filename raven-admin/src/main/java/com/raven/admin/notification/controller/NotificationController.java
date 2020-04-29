package com.raven.admin.notification.controller;

import com.raven.admin.messages.bean.param.ReqMsgParam;
import com.raven.admin.notification.service.NotificationService;
import com.raven.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/notification", produces = APPLICATION_JSON_VALUE)
public class NotificationController {

    @Autowired
    private NotificationService service;

    /**
     * notification sent to user.
     */
    @PostMapping("/user")
    public Result send2User(@RequestBody ReqMsgParam param) {
        log.info("send2User, fromUid {}, targetUid {}", param.getFromUid(), param.getTargetUid());
        return service.notify2User(param);
    }

    /**
     * notification sent to conversation.
     */
    @PostMapping("/conversation")
    public Result send2Conversation(@RequestBody ReqMsgParam param) {
        log.info("send2Conversation, fromUid {}, conversation id {}", param.getFromUid(), param.getTargetUid());
        return service.notify2Conversation(param);
    }
}
