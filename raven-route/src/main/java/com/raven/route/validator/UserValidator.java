package com.raven.route.validator;

import com.raven.common.result.ResultCode;
import org.springframework.stereotype.Component;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: UserValidator
 **/
@Component
public class UserValidator implements Validator {


    @Override
    public boolean validate(String key) {
        // TODO  not support register.
        return true;
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.USER_ERROR_UID_NOT_EXISTS;
    }
}
