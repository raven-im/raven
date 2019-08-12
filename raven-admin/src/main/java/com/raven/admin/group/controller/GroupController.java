package com.raven.admin.group.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.raven.admin.group.bean.param.GroupOutParam;
import com.raven.admin.group.bean.param.GroupReqParam;
import com.raven.admin.group.service.GroupService;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/group", produces = APPLICATION_JSON_VALUE)
public class GroupController {

    @Autowired
    private GroupService service;

    @PostMapping
    public Result create(@RequestBody GroupReqParam param) {
        log.info("group create name:{}", param.getName());
        return service.createGroup(param);
    }

    @PostMapping("/join")
    public Result join(@RequestBody GroupReqParam param) {
        log.info("group join:{}", param.getGroupId());
        return service.joinGroup(param);
    }

    @PostMapping("/quit")
    public Result quit(@RequestBody GroupReqParam param) {
        log.info("group quit:{}", param.getGroupId());
        return service.quitGroup(param);
    }

    @DeleteMapping("/{id}")
    public Result dismiss(@PathVariable("id") String groupId) {
        log.info("group dismiss:{}", groupId);
        return service.dismissGroup(groupId);
    }

    @GetMapping("/{id}")
    public Result detailsGet(@PathVariable("id") String groupId) {
        log.info("group details:{}", groupId);
        return service.groupDetail(groupId);
    }

    @PostMapping("/{id}")
    public Result detailsPost(@PathVariable("id") String groupId) {
        log.info("group details:{}", groupId);
        return service.groupDetail(groupId);
    }
}
