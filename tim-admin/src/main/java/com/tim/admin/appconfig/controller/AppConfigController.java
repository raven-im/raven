package com.tim.admin.appconfig.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.admin.appconfig.bean.model.AppConfigModel;
import com.tim.admin.appconfig.bean.param.AppConfigOutParam;
import com.tim.admin.appconfig.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/app", produces = APPLICATION_JSON_VALUE)
@Slf4j
public class AppConfigController {

	private AppConfigService service;

    @Autowired
    public AppConfigController(AppConfigService service) {
        this.service = service;
    }

    @PutMapping
    public @ResponseBody Result createApp() {
        log.info("admin app create.");
        return Result.success(new AppConfigOutParam(service.createApp()));
    }

    @DeleteMapping("/{uid}")
    public @ResponseBody Result deleteApp(@PathVariable("uid") String uid) {
        log.info("admin app delete . uid {}", uid);
        service.delApp(uid);
        return Result.success();
    }

    @GetMapping("/{uid}")
    public @ResponseBody Result getApp(@PathVariable("uid") String uid) {
        log.info("admin app query . uid {}", uid);
        AppConfigModel model = service.getApp(uid);
        if (model == null) {
            return Result.failure(ResultCode.COMMON_INVALID_PARAMETER);
        }
        return Result.success(new AppConfigOutParam(model));
    }
}
