package com.tim.access.offline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tim.common.utils.Constants;
import com.tim.common.utils.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class OfflineMsgService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


}
