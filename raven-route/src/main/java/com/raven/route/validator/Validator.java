package com.raven.route.validator;

import com.raven.common.result.ResultCode;

public interface Validator {
    boolean validate(String key);
    ResultCode errorCode();
}
