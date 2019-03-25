package com.tim.admin.common;

import com.tim.common.result.RestResult;
import com.tim.common.result.ResultCode;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(BaseException.class)
    public @ResponseBody
    RestResult handleValidationException(BaseException baseException) {
        log.error(baseException.getMessage());
        return new RestResult().setRspCode(ResultCode.COMMON_SERVER_ERROR.getCode())
            .setRspMsg(baseException.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public @ResponseBody
    RestResult handleSQLException(SQLException sqlException) {
        log.error(sqlException.getMessage());
        sqlException.printStackTrace();
        return RestResult.failure();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public @ResponseBody
    RestResult handleMethodNotSupportException(HttpRequestMethodNotSupportedException exception) {
        log.error(exception.getMessage());
        return RestResult.generate(ResultCode.COMMON_METHOD_NOT_SUPPORT);
    }

    @ExceptionHandler(Exception.class)
    public @ResponseBody
    RestResult handleException(Exception exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        return RestResult.failure();
    }
}
