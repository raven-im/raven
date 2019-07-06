package com.raven.admin.gateway.controller;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static com.raven.common.utils.Constants.AUTH_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.raven.admin.gateway.service.GatewayService;
import com.raven.common.enums.GatewayServerType;
import com.raven.common.param.InTokenParam;
import com.raven.common.result.Result;
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
@RequestMapping(value = "/gateway", produces = APPLICATION_JSON_VALUE)
public class GatewayController {

    @Autowired
    private GatewayService userService;

    /**
     * 获取用户登录合法token
     */
    @GetMapping("/token")
    public Result getToken(@RequestHeader(AUTH_APP_KEY) String appKey,
        @RequestParam("uid") String uid) {
        log.info("get token, app key {}, uid {}", appKey, uid);
        return userService.getToken(uid, appKey);
    }

    @PostMapping("/token")
    public Result getToken(@RequestBody InTokenParam param) {
        log.info("get token, app key {}, uid {}", param.getAppKey(), param.getUid());
        return userService.getToken(param.getUid(), param.getAppKey());
    }

    /**
     * 获取网关websocket接入地址
     */
    @GetMapping("/ws")
    public Result getGatewaySiteWs(@RequestHeader(AUTH_TOKEN) String token) {
        log.info("get gateway site, token {}", token);
        return userService.getGatewaySite(token, GatewayServerType.WEBSOCKET);
    }

    /**
     * 获取网关tcp接入地址
     */
    @GetMapping
    public Result getGatewaySite(@RequestHeader(AUTH_TOKEN) String token) {
        log.info("get gateway site, token {}", token);
        return userService.getGatewaySite(token, GatewayServerType.TCP);
    }
}
