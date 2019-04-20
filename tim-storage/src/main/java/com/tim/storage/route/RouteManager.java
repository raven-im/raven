package com.tim.storage.route;


import static com.tim.common.utils.Constants.USER_ROUTE_KEY;

import com.tim.common.loadbalance.Server;
import com.tim.common.model.ConverInfo;
import com.tim.common.utils.Constants;
import com.tim.common.utils.JsonHelper;
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
        redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).put(uid, server);
        redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server).add(uid);
    }

    // 移除路由
    public void removerUserFromServer(String uid, Server server) {
        redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).delete(uid);
        redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server)
            .remove(uid);
    }

    // 服务下线
    public void serverDown(Server server) {
        Cursor cursor = redisTemplate.boundSetOps(Constants.ACCESS_SERVER_ROUTE_KEY + server)
            .scan(ScanOptions.scanOptions().count(Long.MAX_VALUE).build());
        while (cursor.hasNext()) {
            String uid = (String) cursor.next();
            redisTemplate.boundHashOps(Constants.USER_ROUTE_KEY).delete(uid);
        }
        redisTemplate.delete(Constants.ACCESS_SERVER_ROUTE_KEY + server);
    }

    // 获取用户路由信息
    public Server getServerByUid(String uid) {
        Object ob = redisTemplate.boundHashOps(USER_ROUTE_KEY).get(uid);
        if (null == ob) {
            return null;
        }
        return JsonHelper.readValue(ob.toString(), Server.class);
    }

}
