package com.raven.route.user.controller;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static com.raven.common.utils.Constants.AUTH_TOKEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.raven.common.enums.AccessServerType;
import com.raven.common.param.GetAccessParam;
import com.raven.common.param.GetTokenParam;
import com.raven.common.result.Result;
import com.raven.route.user.service.UserService;
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
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private UserService userService;

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
    public Result getToken(@RequestBody GetTokenParam param) {
        log.info("get token, app key {}, uid {}", param.getAppKey(), param.getUid());
        return userService.getToken(param.getUid(), param.getAppKey());
    }

    /**
     * 获取用户登录websocket接入点
     */
    @GetMapping("/access/web")
    public Result getAccessInfoWeb(@RequestHeader(AUTH_TOKEN) String token) {
        log.info("get access address, token {}", token);
        return userService.getAccessInfo(token, AccessServerType.WEBSOCKET);
    }

    /**
     * 获取用户登录tcp接入点
     */
    @GetMapping("/access")
    public Result getAccessInfo(@RequestHeader(AUTH_TOKEN) String token) {
        log.info("get access address, token {}", token);
        return userService.getAccessInfo(token, AccessServerType.TCP);
    }
}
