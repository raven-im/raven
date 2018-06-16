package cn.timmy.message;

import cn.timmy.common.redis.RedisPoolManager;
import cn.timmy.common.utils.SnowFlake;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author zxx
 * Description 消息服务启动器
 * Date Created on 2018/5/25
 */
public class MessageStarter {

    private static final Logger logger = LogManager.getLogger(MessageStarter.class);
    public static RedisPoolManager redisPoolManager;
    public static SnowFlake SnowFlake;

    public static void main(String[] args) throws Exception {
        configAndStart();
    }

    private static void configAndStart() throws Exception {
        Properties prop = new Properties();
        ClassLoader classLoader = MessageStarter.class.getClassLoader();
        // 获取到package下的文件
        prop.load(classLoader.getResourceAsStream("message.properties"));
        int messageListenPort = Integer.parseInt(prop.getProperty("message.server.port"));
        redisPoolManager = new RedisPoolManager();
        redisPoolManager.REDIS_SERVER = prop.getProperty("redis.ip");
        redisPoolManager.REDIS_PORT = Integer.parseInt(prop.getProperty("redis.port"));
        redisPoolManager.getJedis();
        SnowFlake = new SnowFlake(1, 1);
        // Start Servers
        new Thread(() -> MessageServer.startMessageServer(messageListenPort)).start();
    }

}
