package cn.timmy.common.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/28
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

    private static String base64Encode(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).putLong(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

}
