package auth.starter;

import auth.AuthLogicConnection;
import auth.AuthServer;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thirdparty.redis.utils.RedisPoolManager;

/**
 * Created by Qzy on 2016/1/28.
 */

public class AuthStarter {

    private static final Logger logger = LoggerFactory.getLogger(AuthStarter.class);
    private static String cfg = "auth/src/main/resources/auth.properties";
    public static RedisPoolManager redisPoolManager;
    public static int workNum = 1;

    public static void main(String[] args) throws Exception {

        configAndStart(args);
    }

    private static void configAndStart(String[] args) throws ParseException {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(cfg));
            int authListenPort = Integer.parseInt(prop.getProperty("server.port"));
            workNum = Integer.parseInt(prop.getProperty("server.workNum"));
            auth.Worker.startWorker(workNum);
            logger.info("AuthServer authListenPort " + authListenPort);
            redisPoolManager = new RedisPoolManager();
            redisPoolManager.REDIS_SERVER = prop.getProperty("redis.ip");
            redisPoolManager.REDIS_PORT = Integer.valueOf(prop.getProperty("redis.port"));
            redisPoolManager.returnJedis(redisPoolManager.getJedis());
            logger.info("Redis init success");
            String logicIp = prop.getProperty("logic.ip");
            int logicPort = Integer.parseInt(prop.getProperty("logic.port"));
            //Now Start Servers
            new Thread(() -> AuthServer.startAuthServer(authListenPort)).start();
            new Thread(() -> AuthLogicConnection.startAuthLogicConnection(logicIp, logicPort))
                    .start();
        } catch (Exception e) {
            logger.error("init cfg error");
            e.printStackTrace();
        }
    }
}
