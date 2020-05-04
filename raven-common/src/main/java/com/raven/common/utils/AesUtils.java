package com.raven.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class AesUtils {

    private static final String ALGORITHM = "AES";

    private static Cipher cipher;
    private static SecretKey key;

    static {
        KeyGenerator keygen = null;
        try {
            keygen = KeyGenerator.getInstance(ALGORITHM);
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(Constants.TOKEN_ENCRYPT_KEY.getBytes());
            keygen.init(128, random);
            SecretKey original_key = keygen.generateKey();
            byte[] raw = original_key.getEncoded();
            key = new SecretKeySpec(raw, ALGORITHM);
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

    }

    public static String AESEncode(String content) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] byte_encode = content.getBytes(StandardCharsets.UTF_8);
        byte[] byte_AES = cipher.doFinal(byte_encode);
        return new String(Base64.encodeBase64(byte_AES), StandardCharsets.UTF_8);
    }

    public static String AESDecode(String content) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] byte_content = Base64.decodeBase64(content);
        byte[] byte_decode = cipher.doFinal(byte_content);
        return new String(byte_decode, StandardCharsets.UTF_8);
    }

}
