package message;

import common.redis.RedisPoolManager;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeaageStarter {

    private static final Logger logger = LoggerFactory.getLogger(MeaageStarter.class);
    private static String cfg = "message/src/main/resources/message.properties";
    public static RedisPoolManager redisPoolManager;

    public static void main(String[] args) throws Exception {
        configAndStart();
    }

    private static void configAndStart() throws Exception {
        Properties prop = new Properties();
        File file = new File(cfg);
        if (file.exists()) {
            prop.load(new FileInputStream(cfg));
        } else {
            ClassLoader classLoader = MeaageStarter.class.getClassLoader();
            // 获取到package下的文件
            prop.load(classLoader.getResourceAsStream("message.properties"));
        }
        int messageListenPort = Integer.parseInt(prop.getProperty("message.server.port"));
        redisPoolManager = new RedisPoolManager();
        redisPoolManager.REDIS_SERVER = prop.getProperty("redis.ip");
        redisPoolManager.REDIS_PORT = Integer.parseInt(prop.getProperty("redis.port"));
        redisPoolManager.returnJedis(redisPoolManager.getJedis());
        logger.info("Redis init successed");
        // Start Servers
        new Thread(() -> MessageServer.startMessageServer(messageListenPort)).start();
    }

}
