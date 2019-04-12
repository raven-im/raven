package com.tim.group.restful.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tim.common.result.Result;
import com.tim.group.restful.bean.param.GroupOutParam;
import com.tim.group.restful.bean.param.GroupReqParam;
import com.tim.group.restful.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/group", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@Slf4j
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping("/create")
    public Result create(@RequestBody GroupReqParam param) {
        log.info("group create name:{}", param.getName());
        return Result.success(new GroupOutParam(groupService.createGroup(param)));
    }

    @PostMapping("/join")
    public Result join() {
        log.info("group join:{}");
        return Result.success();
    }

    @PostMapping("/quit")
    public Result quit() {
        log.info("group join:{}");
        return Result.success();
    }

    @PostMapping("/dismiss")
    public Result dismiss() {
        log.info("group dismiss:{}");
        return Result.success();
    }

    @PostMapping("/detail")
    public Result details() {
        log.info("group details:{}");
        return Result.success();
    }
}
