package com.tim.common.utils;

/**
 * Author zxx Description 常量 Date Created on 2018/6/4
 */
public class Constants {

    public static final String OFF_USER_MSG_KEY = "offline_user_msg_";

    public static final String TIM_OFFLINE_MESSAGE = "tim_offline_message_";

    public static final String TIM_WAIT_ACK_MESSAGE = "tim_wait_ack_message_";

    public static final String OFF_NOTIFY_KEY = "off_notify_";

    public static final String NOTIFY_TYPE_USER = "notify_user_";

    public static final String NOTIFY_TYPE_GROUP = "notify_group_";

    /*
     * Authentication.
     * */
    public static final String AUTH_APP_KEY = "AppKey";

    public static final String AUTH_NONCE = "Nonce";

    public static final String AUTH_TIMESTAMP = "Timestamp";

    public static final String AUTH_SIGNATURE = "Sign";

    public static final String AUTH_TOKEN = "token";

    public static final long TOKEN_CACHE_DURATION = 7; // 7 days

    /**
     * Default cipher algorithm
     */
    public static final String DEFAULT_CIPHER_ALGORITHM = "DES";

    /**
     * Default separate sign.
     */
    public static final String DEFAULT_SEPARATES_SIGN = ":";
}
