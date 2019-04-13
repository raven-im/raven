package com.tim.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
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
        return mapper.writeValueAsBytes(object);
    }

    public static Map<String, Object> strToMap(String str) throws IOException {
        try {
            return mapper.readValue(str, Map.class);
        } catch (IOException e) {
            log.error("read json value error");
        }
        return null;
    }

    public static <T> T readValue(String str, Class<T> classType) {
        try {
            return mapper.readValue(str, classType);
        } catch (IOException e) {
            log.error("read json value error");
        }
        return null;
    }
}
