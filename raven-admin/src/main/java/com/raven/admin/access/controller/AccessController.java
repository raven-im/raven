package com.raven.admin.access.controller;

import com.raven.admin.access.service.AccessService;
import com.raven.common.enums.GatewayServerType;
import com.raven.common.param.InTokenParam;
import com.raven.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static com.raven.common.utils.Constants.AUTH_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/access", produces = APPLICATION_JSON_VALUE)
public class AccessController {

    @Autowired
    private AccessService accessService;

    /**
     * 获取用户登录合法token
     */
    @GetMapping("/token")
    public Result getToken(@RequestHeader(AUTH_APP_KEY) String appKey,
                           @RequestParam("uid") String uid,
                           @RequestParam("deviceId") String deviceId) {
        log.info("get token, app key {}, uid {}, deviceId {}", appKey, uid, deviceId);
        return accessService.getToken(appKey, uid, deviceId);
    }

    @PostMapping("/token")
    public Result getToken(@RequestBody InTokenParam param) {
        log.info("get token, app key {}, uid {}, deviceId {}", param.getAppKey(), param.getUid(), param.getDeviceId());
        return accessService.getToken(param.getAppKey(), param.getUid(), param.getDeviceId());
    }

    /**
     * 获取网关websocket接入地址
     */
    @GetMapping("/ws")
    public Result getGatewaySiteWs(@RequestHeader(AUTH_TOKEN) String token) {
        log.info("get gateway site, token {}", token);
        return accessService.getGatewaySite(token, GatewayServerType.WEBSOCKET);
    }

    /**
     * 获取网关tcp接入地址
     */
    @GetMapping(("/socket"))
    public Result getGatewaySite(@RequestHeader(AUTH_TOKEN) String token) {
        log.info("get gateway site, token {}", token);
        return accessService.getGatewaySite(token, GatewayServerType.TCP);
    }
}
