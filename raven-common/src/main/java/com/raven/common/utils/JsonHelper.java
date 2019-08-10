package com.raven.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonHelper {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static final JsonFormat.Parser parser = JsonFormat.parser().ignoringUnknownFields();

    public static final JsonFormat.Printer printer = JsonFormat.printer().includingDefaultValueFields().printingEnumsAsInts();

    public static String toJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("to json string error", e);
        }
        return null;
    }

    public static byte[] toJsonBytes(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("to json bytes error", e);
        }
        return null;
    }

    public static Map<String, Object> strToMap(String str) {
        try {
            return mapper.readValue(str, Map.class);
        } catch (IOException e) {
            log.error("read json value error", e);
        }
        return null;
    }

    public static <T> T readValue(String str, Class<T> classType) {
        try {
            return mapper.readValue(str, classType);
        } catch (IOException e) {
            log.error("read json value error", e);
        }
        return null;
    }

    public static void readValue(String json, Message.Builder builder) {
        try {
            parser.merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            log.error("read pb json value error", e);
        }
    }

    public static String toJsonString(MessageOrBuilder message) {
        try {
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            log.error("pb to json string error", e);
        }
        return null;
    }

}
