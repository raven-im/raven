package com.tim.common.utils;

/**
 * Author zxx
 * Description 消息协议号
 * Date Created on 2018/5/12
 */
public class MessageTypeConstants {

    public static final int LOGIN = 1000; // 登录
    public static final int LOGINACK = 1001; // 登录回复
    public static final int UPSINGLE = 1002;
    public static final int DOWNSINGLE = 1003;
    public static final int UPGROUP = 1004;
    public static final int DOWNGROUP = 1005;
    public static final int MESSAGEACK = 1006;
    public static final int HEARTBEAT = 1007;
    public static final int NOTIFY = 1008;

}
