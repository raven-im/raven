package cn.timmy.logic.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ExceptionController {

    private static final Logger logger = LogManager.getLogger(
        ExceptionController.class);

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception exception) {
        logger.error("handle excption:{}", exception.getClass().getName());
        return Result.failure(ResultCode.ERROR, exception.getMessage());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result handleRuntimeException(RuntimeException exception) {
        logger.error("handle excption:{}", exception.getClass().getName());
        return Result.failure(ResultCode.ERROR, exception.getMessage());
    }

}
