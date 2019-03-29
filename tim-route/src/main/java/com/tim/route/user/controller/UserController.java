package com.tim.route.user.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tim.common.result.Result;
import com.tim.route.config.security.SecurityUtils;
import com.tim.route.user.bean.param.ChangePasswordParam;
import com.tim.route.user.bean.param.LoginInputParam;
import com.tim.route.user.bean.param.RegisterParam;
import com.tim.route.user.service.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author zxx
 * Description 用户接口
 * Date Created on 2018/6/12
 */
@RestController
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterParam param) {
        log.info("name:{} register username:{}", param.getName(), param.getUsername());
        return userService.register(param);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(HttpServletRequest request, HttpServletResponse response,
        @RequestBody LoginInputParam param) {
        log.info("username:{} login", param.getUsername());
        return userService.login(request, response, param);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("uid:{} logout", SecurityUtils.getUid());
        return userService.logout(request, response);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result changePassword(@RequestBody ChangePasswordParam param) {
        log.info("uid:{} change password", SecurityUtils.getUid());
        return userService.changePassword(param);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{uid}")
    public Result getUserInfo(@PathVariable("uid") String uid) {
        log.info("uid:{} get uid:{} info", SecurityUtils.getUid(), uid);
        return userService.getUserInfo(uid);
    }

}
