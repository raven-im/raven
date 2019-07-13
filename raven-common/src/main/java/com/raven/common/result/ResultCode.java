package com.raven.common.result;

/**
 * Author zxx Description 返回参数 Date Created on 2018/6/12
 */
public enum ResultCode {

    COMMON_SUCCESS(10000, "success"),
    COMMON_SERVER_ERROR(10001, "Server Failed!"),
    COMMON_NOT_IMPLEMENT(10002, "not implement"),
    COMMON_INVALID_PARAMETER(10003, "invalid parameter."),
    COMMON_NOT_FOUND(10004, "not found"),
    COMMON_METHOD_NOT_SUPPORT(10005, "http method not support."),
    COMMON_SIGN_ERROR(10006, "signature not passed."),
    COMMON_UNAUTHORIZED_ERROR(10007, "unauthorized."),
    COMMON_NO_GATEWAY_ERROR(10008, "no gateway."),
    COMMON_KAFKA_PRODUCE_ERROR(10009, "kafka produce error."),
    COMMON_SERVER_NOT_AVAILABLE(10010, "server not available."),

    /*
     * USER PART ERROR.  [11000, 12000)
     */
    USER_ERROR_COMMON_ERROR(11000, "user module common error."),
    USER_ERROR_UID_NOT_EXISTS(11001, "user id not exists."),

    /*
     * GROUP PART ERROR.  [12000, 13000)
     */
    GROUP_ERROR_COMMON_ERROR(12000, "group module common error."),
    GROUP_ERROR_INVALID_GROUP_ID(12001, "group id invalid."),
    GROUP_ERROR_MEMBER_NOT_IN(12002, "member not in group."),
    GROUP_ERROR_MEMBER_ALREADY_IN(12003, "member already in group."),
    /*
     * APP PART ERROR.  [13000, 14000)
     */
    APP_ERROR_COMMON_ERROR(13000, "app module common error."),
    APP_ERROR_KEY_INVALID(13001, "app key is invalid."),
    APP_ERROR_TOKEN_CREATE_ERROR(13002, "token cannot be made."),
    APP_ERROR_TOKEN_INVALID(13003, "token is invalid."),

    /*
     * File Upload.  [14000, 15000)
     */
    UPLOAD_FILE_COMMON_ERROR(14000, "File Upload common error."),
    UPLOAD_FILE_EMPTY(14001, "File empty."),
    UPLOAD_FILE_UPLOAD_PARAMETER_ERROR(14002, "File upload Parameters error."),
    UPLOAD_FILE_UPLOAD_ERROR(14003, "File upload error."),

    COMMON_ERROR(500, "error");
    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(int code) {
        for (ResultCode r : ResultCode.values()) {
            if (r.getCode() == code) {
                return r.getMsg();
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean success() {
        return getCode() == COMMON_SUCCESS.getCode();
    }
}
