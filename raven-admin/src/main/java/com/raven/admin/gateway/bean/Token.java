package com.raven.admin.gateway.bean;

import static com.raven.common.utils.Constants.DEFAULT_SEPARATOR;

import com.raven.common.exception.TokenException;
import com.raven.common.utils.DesUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Data;

@Data
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
        String token = uid + DEFAULT_SEPARATOR + timestamp + DEFAULT_SEPARATOR + appKey;
        byte[] encryptCode = DesUtils.encrypt(secret.getBytes(), token.getBytes());
        return new String(Base64.getEncoder().encode(encryptCode), StandardCharsets.UTF_8);
    }
}
