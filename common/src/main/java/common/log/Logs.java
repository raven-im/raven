package common.log;

import static ch.qos.logback.classic.util.ContextInitializer.CONFIG_FILE_PROPERTY;

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
        System.setProperty(CONFIG_FILE_PROPERTY, "common/src/main/resources/logback.xml");
        return true;
    }

}
