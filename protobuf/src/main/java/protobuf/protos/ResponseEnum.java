package protobuf.protos;

/**
 * Author zxx Description 登录响应 Date Created on 2018/5/26
 */
public enum ResponseEnum {

    SUCCESS(200, "success"),
    FAIL(400, "fail");

    ResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public final int code;
    public final String msg;
}
