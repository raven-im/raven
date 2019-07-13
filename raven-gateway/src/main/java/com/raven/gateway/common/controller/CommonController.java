package com.raven.gateway.common.controller;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static com.raven.common.utils.Constants.AUTH_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.raven.common.enums.GatewayServerType;
import com.raven.common.param.InTokenParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/", produces = APPLICATION_JSON_VALUE)
public class CommonController {

    /**
     *  服务不可用统一放回
     */
    @GetMapping("/fallback")
    public Result fallback() {
        return Result.failure(ResultCode.COMMON_SERVER_NOT_AVAILABLE);
    }
}
