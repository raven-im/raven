package com.tim.common.result;

/**
 * Author zxx
 * Description 返回参数
 * Date Created on 2018/6/12
 */
public enum ResultCode {

    COMMON_SUCCESS(10000, "success"),
    COMMON_SERVER_ERROR(10001, "Server Failed!"),
    COMMON_NOT_IMPLEMENT(10002, "not implement"),
    COMMON_INVALID_PARAMETER(10003, "invalid parameter."),
    COMMON_NOT_FOUND(10004, "not found"),
    COMMON_METHOD_NOT_SUPPORT(10005, "http method not support."),
    COMMON_SIGN_ERROR(10006, "signature not passed."),

    /*
     * USER PART ERROR.  [11000, 12000)
     */
    USER_ERROR_COMMON_ERROR(11000, "user module common error."),
    USER_ERROR_UID_NOT_EXISTS(11001, "user id not exists."),

    /*
     * GROUP PART ERROR.  [12000, 13000)
     */
    GROUP_ERROR_COMMON_ERROR(12000, "group module common error."),

    /*
     * APP PART ERROR.  [13000, 14000)
     */
    APP_ERROR_COMMON_ERROR(13000, "app module common error."),
    APP_ERROR_KEY_INVALID(13001, "app key is invalid."),
    APP_ERROR_TOKEN_CREATE_ERROR(13002, "token cannot be made."),

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
