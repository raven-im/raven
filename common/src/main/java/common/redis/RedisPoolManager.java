package common.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Author zxx
 * Description Redis连接池管理器
 * Date Created on 2018/5/25
 */
public class RedisPoolManager {

    private static final Logger logger = LogManager.getLogger(RedisPoolManager.class);

    public String REDIS_SERVER = "localhost";
    public int REDIS_PORT = 6379;
    private JedisPool pool = null;

    private JedisPool getInstance() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(1000);
            config.setMaxIdle(20);
            config.setMaxWaitMillis(10 * 1000);
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, REDIS_SERVER, REDIS_PORT, 10);
        }
        return pool;
    }

    /**
     * 获取jedis
     */
    public Jedis getJedis() {
        Jedis jedis = null;
        try {
            jedis = getInstance().getResource();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return jedis;
    }
}
