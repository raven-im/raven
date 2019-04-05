package com.tim.common.enums;

import lombok.Data;

/**
 * Author zxx Description 登录响应 Date Created on 2018/5/26
 */
public enum LoginResult {

    SUCCESS(200, "success"),
    FAIL(500, "fail");

    LoginResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public final int code;

    public final String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }}
