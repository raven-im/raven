package com.raven.admin.app.controller;

import com.raven.admin.app.bean.model.AppRegModel;
import com.raven.admin.app.service.AppRegService;
import com.raven.common.param.OutAppConfigParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(path = "/app", produces = APPLICATION_JSON_VALUE)
public class AppRegController {

    @Autowired
    private AppRegService service;

    @PutMapping
    public Result createApp() {
        log.info("admin app create.");
        AppRegModel model = service.createApp();
        return Result.success(new OutAppConfigParam(model.getUid(), model.getSecret()));
    }

    @DeleteMapping("/{uid}")
    public Result deleteApp(@PathVariable("uid") String uid) {
        log.info("admin app delete . uid {}", uid);
        service.delApp(uid);
        return Result.success();
    }

    @GetMapping("/{uid}")
    public Result getApp(@PathVariable("uid") String uid) {
        log.info("admin app query . uid {}", uid);
        AppRegModel model = service.getApp(uid);
        if (model == null) {
            return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
        }
        return Result.success(new OutAppConfigParam(model.getUid(), model.getSecret()));
    }
}
