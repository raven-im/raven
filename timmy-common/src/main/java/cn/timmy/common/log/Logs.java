package cn.timmy.common.log;

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

}
