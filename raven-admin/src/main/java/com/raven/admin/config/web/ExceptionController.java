package com.raven.admin.config.web;

import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Author zxx
 * Description 全局异常处理
 * Date Created on 2018/6/12
 */
@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception exception) {
        log.error("handle exception:{}", exception);
        return Result.failure(ResultCode.COMMON_ERROR, exception.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result handleRuntimeException(RuntimeException exception) {
        log.error("handle exception:{}", exception);
        return Result.failure(ResultCode.COMMON_ERROR, exception.getMessage());
    }

}
