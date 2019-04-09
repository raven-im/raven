package com.tim.common.uuid;

import static junit.framework.TestCase.assertEquals;
import com.tim.common.TimCommonTest;
import com.tim.common.utils.UidUtil;
import org.junit.Test;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: DesCipherTest
 **/
public class UuidTest extends TimCommonTest {

    @Test
    public void testUuid1Factor() throws Exception {
        String uid = UidUtil.uuid();
        String first = UidUtil.uuid24ByFactor(uid);
        String second = UidUtil.uuid24ByFactor(uid);

        assertEquals(first, second);
    }

    @Test
    public void testUuid2Factor() throws Exception {
        String uid1 = UidUtil.uuid();
        String uid2 = UidUtil.uuid();
        String first = UidUtil.uuid24By2Factor(uid1, uid2);
        String second = UidUtil.uuid24By2Factor(uid2, uid1);

        assertEquals(first, second);
    }
}
