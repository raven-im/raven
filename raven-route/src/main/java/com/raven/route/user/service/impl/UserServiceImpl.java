package com.raven.route.user.service.impl;

import com.raven.common.exception.TokenException;
import com.raven.common.loadbalance.ConsistentHashLoadBalancer;
import com.raven.common.loadbalance.LoadBalancer;
import com.raven.common.loadbalance.Server;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.route.validator.AppKeyValidator;
import com.raven.route.validator.TokenValidator;
import com.raven.route.validator.UserValidator;
import com.raven.route.user.bean.model.AppConfigModel;
import com.raven.route.user.bean.param.*;
import com.raven.route.user.mapper.AppConfigMapper;
import com.raven.route.user.service.UserService;
import com.raven.storage.route.RouteManager;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.raven.route.utils.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.raven.common.utils.Constants.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private AppConfigMapper configMapper;

    @Autowired
    private AppKeyValidator appKeyValidator;

    @Autowired
    private TokenValidator tokenValidator;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RouteManager routeManager;

    @Override
    public Result getToken(String uid, String appKey) {
        // check app key validation.
        if (!appKeyValidator.validate(appKey)) {
            return Result.failure(appKeyValidator.errorCode());
        }
        // check uid validation.
        if (!userValidator.validate(uid)) {
            return Result.failure(userValidator.errorCode());
        }
        //  uid:timestamp:appkey => DES(appSecret) => BASE64 => token
        try {
            String token = new Token(uid, appKey).getToken(getAppSecret(appKey));

            // cache token to redis.
            String key = appKey + DEFAULT_SEPARATES_SIGN + uid;
            redisTemplate.boundHashOps(token).put(token, key);
            redisTemplate.boundHashOps(token).expire(TOKEN_CACHE_DURATION, TimeUnit.DAYS);
//            redisTemplate.opsForValue().set(token, key, TOKEN_CACHE_DURATION, TimeUnit.DAYS);

            return Result.success(new TokenInfoOutParam(appKey, uid, token));
        } catch (TokenException e) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_CREATE_ERROR);
        }
    }

    @Override
    public Result getAccessInfo(String key, String token) {
        // check app key validation.
        if (!appKeyValidator.validate(key)) {
            return Result.failure(appKeyValidator.errorCode());
        }
        // check token validation.
        if (!tokenValidator.validate(token)) {
            return Result.failure(tokenValidator.errorCode());
        }
        String tokenStr = (String) redisTemplate.boundHashOps(token).get(token);
        String uid = tokenStr.split(DEFAULT_SEPARATES_SIGN)[1];

        // check if there is already a Access server.  if yes , dispatch to that server.
        Server server = routeManager.getServerByUid(uid);
        if (null != server) {
            return Result
                .success(new ServerInfoOutParam(key, uid, server.getIp(), server.getPort()));
        } else {
            List<ServiceInstance> instances = discoveryClient
                .getInstances(CONFIG_ACCESS_SERVER_NAME);
            if (!instances.isEmpty()) {
                List<Server> servers = instances.stream()
                    .map((x) -> {
                        if (x.getMetadata().containsKey(CONFIG_TCP_PORT)) {
                            int nettyPort = Integer.valueOf(x.getMetadata().get(CONFIG_TCP_PORT));
                            return new Server(x.getHost(), nettyPort);
                        } else {
                            return new Server(x.getHost(), x.getPort());
                        }
                    })
                    .collect(Collectors.toList());
                LoadBalancer lb = new ConsistentHashLoadBalancer();
                Server origin = lb.select(servers, uid);
                return Result
                    .success(new ServerInfoOutParam(key, uid, origin.getIp(), origin.getPort()));
            }
        }
        return Result.failure(ResultCode.COMMON_NO_ACCESS_ERROR);
    }

    private String getAppSecret(String key) {
        AppConfigModel model = new AppConfigModel();
        model.setUid(key);
        AppConfigModel app = configMapper.selectOne(model);
        return app.getSecret();
    }
}
