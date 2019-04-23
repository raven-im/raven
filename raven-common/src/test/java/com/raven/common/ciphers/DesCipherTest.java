package com.raven.common.ciphers;

import static com.raven.common.utils.DesUtils.decrypt;
import static com.raven.common.utils.DesUtils.encrypt;
import static junit.framework.TestCase.assertEquals;

import com.raven.common.RavenCommonTest;
import org.junit.Test;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: DesCipherTest
 **/
public class DesCipherTest extends RavenCommonTest {
    @Test
    public void testDesAlgorithm() throws Exception {
        String key = "GitHub_GitHub_GitHub";
        String expected = "Hello world";

        byte[] encryptCode = encrypt(key.getBytes(), expected.getBytes());

        byte[] decryptCode = decrypt(key.getBytes(), encryptCode);

        String actual = new String(decryptCode, "UTF-8");

        assertEquals(actual, expected);
    }
}
