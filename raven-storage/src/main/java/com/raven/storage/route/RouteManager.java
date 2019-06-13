package com.raven.storage.route;


import static com.raven.common.utils.Constants.USER_ROUTE_KEY;

import com.raven.common.loadbalance.AccessServerInfo;
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
    public void addUser2Server(String uid, AccessServerInfo server) {
        redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).put(uid, server.toString());
        redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server.hashCode()).add(uid);
    }

    // 移除路由
    public void removerUserFromServer(String uid, AccessServerInfo server) {
        redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).delete(uid);
        redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server.hashCode())
            .remove(uid);
    }

    // 服务下线
    public void serverDown(AccessServerInfo server) {
        Cursor cursor = redisTemplate
            .boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server.hashCode())
            .scan(ScanOptions.scanOptions().count(Long.MAX_VALUE).build());
        while (cursor.hasNext()) {
            String uid = (String) cursor.next();
            redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).delete(uid);
        }
        redisTemplate.delete(Constants.ACCESS_SERVER_ROUTE_KEY + server.hashCode());
    }

    // 获取用户路由信息
    public AccessServerInfo getServerByUid(String uid) {
        Object ob = redisTemplate.boundHashOps(USER_ROUTE_KEY).get(uid);
        if (null == ob) {
            return null;
        }
        return JsonHelper
            .readValue(ob.toString(), AccessServerInfo.class);
    }

}
