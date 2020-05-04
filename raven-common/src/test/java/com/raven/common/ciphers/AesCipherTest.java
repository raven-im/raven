package com.raven.common.ciphers;

import com.raven.common.RavenCommonTest;
import com.raven.common.utils.AesUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: AesCipherTest
 **/
@Slf4j
public class AesCipherTest extends RavenCommonTest {
    @Test
    public void testEnDesAlgorithm() throws Exception {
        String expected = "u43tOdeHSx8r0XfJRuRDgo:test2:test2_DEVICE:1588503591881";

        String encryptCode = AesUtils.AESEncode(expected);
        log.info(encryptCode);
        String decryptCode = AesUtils.AESDecode(encryptCode);

        assertEquals(decryptCode, expected);
    }
}
