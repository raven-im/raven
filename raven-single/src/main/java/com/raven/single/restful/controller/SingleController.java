package com.raven.single.restful.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.raven.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
