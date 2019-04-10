package com.tim.single.restful.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tim.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author zxx
 * Description 群组相关接口
 * Date Created on 2018/6/12
 */
@RestController
@RequestMapping(value = "/single", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@Slf4j
public class SingleController {
    @PostMapping("/chat")
    public Result chat() {
        log.info("single chat:{}");
        return Result.success();
    }
}
