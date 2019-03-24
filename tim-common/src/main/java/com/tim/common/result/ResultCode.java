package com.tim.common.result;

/**
 * Author zxx
 * Description 返回参数
 * Date Created on 2018/6/12
 */
public enum ResultCode {

    SUCCESS(200, "sucess"),

    ERROR(500, "error");

    private Integer code;

    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }
}
