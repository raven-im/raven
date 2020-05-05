package com.raven.admin.group.controller;

import com.raven.admin.group.bean.param.GroupOutParam;
import com.raven.admin.group.bean.param.GroupReqParam;
import com.raven.admin.group.service.GroupService;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/group", produces = APPLICATION_JSON_VALUE)
public class GroupController {

    @Autowired
    private GroupService service;

    @PostMapping("/create")
    public Result create(@RequestBody GroupReqParam param,
                         @RequestHeader(value = AUTH_APP_KEY) String appKey) {
        log.info("app key {}, group create name:{}", appKey, param.getName());
        return Result.success(new GroupOutParam(service.createGroup(appKey, param)));
    }

    @PostMapping("/join")
    public Result join(@RequestBody GroupReqParam param,
                       @RequestHeader(value = AUTH_APP_KEY) String appKey) {
        log.info("app key {}, group join:{}", appKey, param.getGroupId());
        ResultCode code = service.joinGroup(appKey, param);
        if (ResultCode.COMMON_SUCCESS.getCode() != code.getCode()) {
            return Result.failure(code);
        }
        return Result.success(code);
    }

    @PostMapping("/quit")
    public Result quit(@RequestBody GroupReqParam param,
                       @RequestHeader(value = AUTH_APP_KEY) String appKey) {
        log.info("app key {}, group quit:{}", appKey, param.getGroupId());
        ResultCode code = service.quitGroup(appKey, param);
        if (ResultCode.COMMON_SUCCESS.getCode() != code.getCode()) {
            return Result.failure(code);
        }
        return Result.success(code);
    }

    @PostMapping("/dismiss")
    public Result dismiss(@RequestBody GroupReqParam param,
                          @RequestHeader(value = AUTH_APP_KEY) String appKey) {
        log.info("app key {}, group dismiss:{}", appKey, param.getGroupId());
        ResultCode code = service.dismissGroup(appKey, param);
        if (ResultCode.COMMON_SUCCESS.getCode() != code.getCode()) {
            return Result.failure(code);
        }
        return Result.success(code);
    }

    @PostMapping("/detail")
    public Result detailsPost(@RequestBody GroupReqParam param,
                              @RequestHeader(value = AUTH_APP_KEY) String appKey) {
        log.info("app key {}, group details:{}", appKey, param.getGroupId());
        return service.groupDetail(appKey, param.getGroupId());
    }

    @GetMapping("/detail")
    public Result detailsGet(@RequestParam("id") String groupId,
                             @RequestHeader(value = AUTH_APP_KEY) String appKey) {
        log.info("app key {}, group details:{}", appKey, groupId);
        return service.groupDetail(appKey, groupId);
    }
}
