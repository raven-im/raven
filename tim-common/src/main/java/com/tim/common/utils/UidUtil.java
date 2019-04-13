package com.tim.common.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * uuid生成工具
 */
public class UidUtil {

    private UidUtil() {
    }

    public static String uuid() {
        return uuid24();
    }

    private static String uuid24() {
        UUID uuid = UUID.randomUUID();
        return base64Encode(uuid.getMostSignificantBits()) + base64Encode(
            uuid.getLeastSignificantBits());
    }

    public static String uuid24By2Factor(String uid1, String uid2) {
        String uid;
        if (uid1.compareTo(uid2) <= 0) {
            uid = uid1 + uid2;
        } else {
            uid = uid2 + uid1;
        }
        UUID uuid = UUID.nameUUIDFromBytes(uid.getBytes());
        return base64Encode(uuid.getMostSignificantBits()) + base64Encode(
            uuid.getLeastSignificantBits());
    }

    public static String uuid24ByFactor(String uid) {
        UUID uuid = UUID.nameUUIDFromBytes(uid.getBytes());
        return base64Encode(uuid.getMostSignificantBits()) + base64Encode(
            uuid.getLeastSignificantBits());
    }

    private static String base64Encode(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).putLong(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

}
