package com.raven.route.validator;

import com.raven.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: TokenValidator
 **/
@Component
public class TokenValidator implements Validator {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean validate(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.APP_ERROR_TOKEN_INVALID;
    }
}
