package com.raven.common.netty;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NettyAttrUtil {

    public static final AttributeKey<String> ATTR_KEY_USER_ID = AttributeKey.valueOf("uid");
    public static final AttributeKey<String> ATTR_KEY_APP_KEY = AttributeKey.valueOf("app_key");
    public static final AttributeKey<String> ATTR_KEY_DEVICE_ID = AttributeKey.valueOf("device_id");
    public static final AttributeKey<String> ATTR_KEY_LOGIN_TIME = AttributeKey.valueOf("login_time");

    public static String getAttribute(Channel channel, AttributeKey<String> key) {
        Attribute<String> attr = channel.attr(key);
        return attr.get();
    }

    public static void setAttrKey(Channel channel, AttributeKey<String> attrKey, String value) {
        channel.attr(attrKey).set(value);
    }
}
