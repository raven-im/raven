package cn.timmy.logic.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Author zxx
 * Description 全局异常处理
 * Date Created on 2018/6/12
 */
@ControllerAdvice
public class ExceptionController {

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception) {
        return new Object();
    }
}
