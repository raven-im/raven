package com.tim.route.utils;

import com.tim.common.utils.DesUtils;
import com.tim.common.exception.TokenException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.tim.common.utils.Constants.DEFAULT_SEPARATES_SIGN;

public class Token {

    private String uid;
    private String appKey;
    private long timestamp;

    public Token(String uid, String appKey) {
        this.uid = uid;
        this.appKey = appKey;
        this.timestamp = System.currentTimeMillis();
    }

    public String getToken(String secret) throws TokenException {
        String token = uid + DEFAULT_SEPARATES_SIGN + timestamp + DEFAULT_SEPARATES_SIGN + appKey;
        byte[] encryptCode = DesUtils.encrypt(secret.getBytes(), token.getBytes());
        return new String(Base64.getEncoder().encode(encryptCode), StandardCharsets.UTF_8);
    }
}
