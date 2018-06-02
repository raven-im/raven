package auth.starter;

import auth.AuthMessageConnection;
import auth.AuthServer;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.redis.RedisPoolManager;

/**
 * Created by Qzy on 2016/1/28.
 */

public class AuthStarter {

    private static final Logger logger = LoggerFactory.getLogger(AuthStarter.class);
    private static String cfg = "auth/src/main/resources/auth.properties";
    public static RedisPoolManager redisPoolManager;
    public static int workNum = 1;

    public static void main(String[] args) throws Exception {
        configAndStart();
    }

    private static void configAndStart() throws Exception {
        Properties prop = new Properties();
        File file = new File(cfg);
        if (file.exists()) {
            prop.load(new FileInputStream(cfg));
        } else {
            ClassLoader classLoader = AuthStarter.class.getClassLoader();
            // 获取到package下的文件
            prop.load(classLoader.getResourceAsStream("auth.properties"));
        }
        int authListenPort = Integer.parseInt(prop.getProperty("auth.port"));
        workNum = Integer.parseInt(prop.getProperty("auth.workNum"));
        auth.Worker.startWorker(workNum);
        redisPoolManager = new RedisPoolManager();
        redisPoolManager.REDIS_SERVER = prop.getProperty("redis.ip");
        redisPoolManager.REDIS_PORT = Integer.valueOf(prop.getProperty("redis.port"));
        redisPoolManager.returnJedis(redisPoolManager.getJedis());
        logger.info("Redis init success");
        String messageIp = prop.getProperty("message.server.ip");
        int messagePort = Integer.parseInt(prop.getProperty("message.server.port"));
        //Now Start Servers
        new Thread(() -> AuthServer.startAuthServer(authListenPort)).start();
        new Thread(() -> AuthMessageConnection.startAuthMessageConnection(messageIp, messagePort))
            .start();
    }
}
