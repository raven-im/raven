package com.tim.route.config.validator;

import com.tim.common.result.ResultCode;

public interface Validator {
    boolean validate(String key);
    ResultCode errorCode();
}
