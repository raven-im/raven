package com.tim.group.restful.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.group.restful.bean.param.GroupOutParam;
import com.tim.group.restful.bean.param.GroupReqParam;
import com.tim.group.restful.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/group", produces = APPLICATION_JSON_VALUE)
@Slf4j
public class GroupController {

    @Autowired
    private GroupService service;

    @PostMapping("/create")
    public Result create(@RequestBody GroupReqParam param) {
        log.info("group create name:{}", param.getName());
        return Result.success(new GroupOutParam(service.createGroup(param)));
    }

    @PostMapping("/join")
    public Result join(@RequestBody GroupReqParam param) {
        log.info("group join:{}", param.getGroupId());
        ResultCode code = service.joinGroup(param);
        if (ResultCode.COMMON_SUCCESS.getCode() != code.getCode()) {
            return Result.failure(code);
        }
        return Result.success(code);
    }

    @PostMapping("/quit")
    public Result quit(@RequestBody GroupReqParam param) {
        log.info("group quit:{}", param.getGroupId());
        ResultCode code = service.quitGroup(param);
        if (ResultCode.COMMON_SUCCESS.getCode() != code.getCode()) {
            return Result.failure(code);
        }
        return Result.success(code);
    }

    @PostMapping("/dismiss")
    public Result dismiss(@RequestBody GroupReqParam param) {
        log.info("group dismiss:{}", param.getGroupId());
        ResultCode code = service.dismissGroup(param);
        if (ResultCode.COMMON_SUCCESS.getCode() != code.getCode()) {
            return Result.failure(code);
        }
        return Result.success(code);
    }

    @PostMapping("/detail")
    public Result detailsPost(@RequestBody GroupReqParam param) {
        log.info("group details:{}", param.getGroupId());
        return service.groupDetail(param.getGroupId());
    }

    @GetMapping("/detail")
    public Result detailsGet(@RequestParam("id") String groupId) {
        log.info("group details:{}", groupId);
        return service.groupDetail(groupId);
    }
}
