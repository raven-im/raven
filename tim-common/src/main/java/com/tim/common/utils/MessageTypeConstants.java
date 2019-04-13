package com.tim.common.utils;

import com.tim.common.protos.History.HisMessagesAck;

/**
 * Author zxx Description 消息协议号 Date Created on 2018/5/12
 */
public class MessageTypeConstants {

    public static final int HEARTBEAT = 1000;
    public static final int LOGIN = 1001; // 登录
    public static final int LOGINACK = 1002; // 登录回复
    public static final int UPDOWNMESSAGE = 1003;
    public static final int MESSAGEACK = 1004;
    public static final int CONVERSATIONREQ = 1005;
    public static final int CONVERSATIONACK = 1006;
    public static final int CONVERSATIONDETAIL = 1007;
    public static final int HISMESSAGESREQ = 1008;
    public static final int HISMESSAGESACK = 1009;
    public static final int NOTIFY = 1010;
    public static final int SERVERINFO = 1011;

}
