package com.tim.common.utils;

import static com.tim.common.utils.Constants.DEFAULT_CIPHER_ALGORITHM;

import com.tim.common.exception.TokenException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: DesUtils
 **/
public class DesUtils {

    private static SecureRandom sr = new SecureRandom();
    private static SecretKeyFactory keyFactory;
    private static KeyGenerator kg;

    static {
        try {
            keyFactory = SecretKeyFactory.getInstance(DEFAULT_CIPHER_ALGORITHM);
            kg = KeyGenerator.getInstance(DEFAULT_CIPHER_ALGORITHM);
            kg.init(sr);
        } catch (NoSuchAlgorithmException neverHappens) {
            neverHappens.printStackTrace();
        }
    }

    /**
     * encrypt
     */
    public static byte[] encrypt(byte[] rawKeyData, byte[] data) throws TokenException {
        try {
            DESKeySpec dks = new DESKeySpec(rawKeyData);
            SecretKey key = keyFactory.generateSecret(dks);

            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, key, sr);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new TokenException(e.getMessage());
        }
    }

    /**
     * decrypt
     */
    public static byte[] decrypt(byte[] rawKeyData, byte[] encryptedData) throws TokenException {

        DESKeySpec dks;
        try {
            dks = new DESKeySpec(rawKeyData);
            SecretKey key = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, key, sr);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new TokenException(e);
        }
    }
}
