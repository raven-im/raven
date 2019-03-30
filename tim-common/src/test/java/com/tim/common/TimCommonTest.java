package com.tim.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;

@Slf4j
public class TimCommonTest {

    @Before
    public void init() {
        log.info("开始测试-----------------");
    }

    @After
    public void after() {
        log.info("测试结束-----------------");
    }

}
