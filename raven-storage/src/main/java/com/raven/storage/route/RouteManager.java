package com.raven.storage.route;


import static com.raven.common.utils.Constants.USER_ROUTE_KEY;

import com.raven.common.loadbalance.Server;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

@Slf4j
public class RouteManager {

    private RedisTemplate redisTemplate;

    public RouteManager(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private RouteManager() {
    }

    // 增加路由
    public void addUser2Server(String uid, Server server) {
        redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).put(uid, server.toString());
        redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server.toString()).add(uid);
    }

    // 增加内部服务路由
    public void addUser2InternalServer(String uid, Server server) {
        redisTemplate.boundHashOps(Constants.USER_INTERNAL_ROUTE_KEY).put(uid, server.toString());
    }

    // 移除路由
    public void removerUserFromServer(String uid, Server server) {
        redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).delete(uid);
        redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server)
            .remove(uid);
    }

    // 移除内部服务路由
    public void removerUserFromInternalServer(String uid, Server server) {
        redisTemplate.boundHashOps(Constants.USER_INTERNAL_ROUTE_KEY).delete(uid);
    }

    // 服务下线
    public void serverDown(Server server) {
        Cursor cursor = redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server)
            .scan(ScanOptions.scanOptions().count(Long.MAX_VALUE).build());
        while (cursor.hasNext()) {
            String uid = (String) cursor.next();
            redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).delete(uid);
            redisTemplate.boundHashOps(Constants.USER_INTERNAL_ROUTE_KEY).delete(uid);
        }
        redisTemplate.delete(Constants.ACCESS_SERVER_ROUTE_KEY + server);
    }

    // 获取用户路由信息
    public Server getServerByUid(String uid) {
        Object ob = redisTemplate.boundHashOps(USER_ROUTE_KEY).get(uid);
        if (null == ob) {
            return null;
        }

        return new Server(ob.toString());
    }

    // 获取内部服务用户路由信息
    public Server getInternalServerByUid(String uid) {
        Object ob = redisTemplate.boundHashOps(Constants.USER_INTERNAL_ROUTE_KEY).get(uid);
        if (null == ob) {
            return null;
        }
        return new Server(ob.toString());
    }

}
