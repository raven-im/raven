package com.tim.route.validator;

import com.tim.common.result.ResultCode;
import com.tim.route.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: UserValidator
 **/
@Component
public class UserValidator implements Validator {

    @Autowired
    private UserMapper userMapper;

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
