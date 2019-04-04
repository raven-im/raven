package com.tim.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Author zxx Description Date Created on 2018/6/4
 */
@Slf4j
public class JsonHelper {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static ObjectMapper getJacksonMapper() {
        return mapper;
    }

    public static String toJsonString(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public static byte[] toJsonBytes(Object object) throws JsonProcessingException {
        return  mapper.writeValueAsBytes(object);
    }

}
