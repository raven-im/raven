package cn.timmy.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author zxx
 * Description log配置
 * Date Created on 2018/5/12
 */
public interface Logs {

    boolean logInit = init();

    static boolean init() {
        if (logInit) {
            return true;
        }
        System.setProperty("log4j.configurationFile",
            "cn/timmy/common/src/main/resources/logback.xml");
        return true;
    }

    Logger LOGIC = LoggerFactory.getLogger("cn.timmt.logic"),

    MESSAGE = LoggerFactory.getLogger("cn.timmy.message"),

    CLIENT = LoggerFactory.getLogger("cn.timmy.client");

}
