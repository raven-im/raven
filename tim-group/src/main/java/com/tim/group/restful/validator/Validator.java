package com.tim.group.restful.validator;

import com.tim.common.result.ResultCode;
import java.util.List;

public interface Validator {
    default boolean isValid(String key) {
        return false;
    }
    default boolean isValid(String key1, List<String> uids) {
        return false;
    }
    default ResultCode errorCode(){
        return ResultCode.COMMON_ERROR;
    }
}
