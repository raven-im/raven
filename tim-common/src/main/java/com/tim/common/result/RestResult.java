package com.tim.common.result;

public class RestResult {
    private Integer rspCode;
    private String rspMsg;
    private Object data;

    public Integer getRspCode() {
        return rspCode;
    }

    public RestResult setRspCode(Integer rspCode) {
        this.rspCode = rspCode;
        return this;
    }

    public String getRspMsg() {
        return rspMsg;
    }

    public RestResult setRspMsg(String rspMsg) {
        this.rspMsg = rspMsg;
        return this;
    }

    public Object getData() {
        return data;
    }

    public RestResult setData(Object data) {
        this.data = data;
        return this;
    }

    public static RestResult success() {
        return new RestResult()
            .setRspCode(ResultCode.COMMON_SUCCESS.getCode())
            .setRspMsg(ResultCode.COMMON_SUCCESS.getMsg());
    }

    public static RestResult success(Object o) {
        return RestResult.success().setData(o);
    }

    public static RestResult failure() {
        return new RestResult()
            .setRspCode(ResultCode.COMMON_SERVER_ERROR.getCode())
            .setRspMsg(ResultCode.COMMON_SERVER_ERROR.getMsg());
    }

    public static RestResult generate(Integer rspCode) {
        return new RestResult().setRspCode(rspCode).setRspMsg(ResultCode.getMsg(rspCode));
    }

    public static RestResult generate(ResultCode result) {
        return new RestResult()
            .setRspCode(result.getCode())
            .setRspMsg(result.getMsg());
    }
}
