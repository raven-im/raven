package com.raven.common.model;

import com.raven.common.exception.TokenException;
import com.raven.common.exception.TokenExceptionType;
import com.raven.common.utils.AesUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.raven.common.utils.Constants.DEFAULT_SEPARATOR;
import static com.raven.common.utils.Constants.TOKEN_EXPIRE_DAY;

@Data
@AllArgsConstructor
@Slf4j
public class Token {

    private String appKey;

    private String uid;

    private String deviceId;

    private long timestamp;

    public String getToken() throws TokenException {
        String token = appKey + DEFAULT_SEPARATOR + uid + DEFAULT_SEPARATOR + deviceId + DEFAULT_SEPARATOR + timestamp;
        try {
            log.info("token string {}", token);
            return AesUtils.AESEncode(token);
        } catch (Exception e) {
            throw new TokenException("token encrypt error.", TokenExceptionType.TOKEN_INVALID);
        }
    }

    public static Token parseFromString(String token) throws TokenException {
        try {
            String decryptToken = AesUtils.AESDecode(token);
            String[] array = decryptToken.split(DEFAULT_SEPARATOR);
            if (array.length != 4) {
                throw new TokenException("parse token error.", TokenExceptionType.TOKEN_INVALID);
            }
            long timestamp = Long.parseLong(array[3]);
            if (System.currentTimeMillis() - timestamp > TOKEN_EXPIRE_DAY) {
                throw new TokenException("token expire.", TokenExceptionType.TOKEN_EXPIRE);
            }

            return new Token(array[0], array[1], array[2], timestamp);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new TokenException("token decrypt error.", TokenExceptionType.TOKEN_INVALID);
        }
    }
}
