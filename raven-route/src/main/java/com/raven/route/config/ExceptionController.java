package com.raven.route.config;

import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception exception) {
        log.error("handle exception", exception);
        return Result.failure(ResultCode.COMMON_SERVER_ERROR, exception.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result handleRuntimeException(RuntimeException exception) {
        log.error("handle runtimeexception", exception);
        return Result.failure(ResultCode.COMMON_SERVER_ERROR, exception.getMessage());
    }

}
