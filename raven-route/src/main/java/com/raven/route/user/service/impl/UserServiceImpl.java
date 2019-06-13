package com.raven.route.user.service.impl;

import static com.raven.common.utils.Constants.CONFIG_ACCESS_SERVER_NAME;
import static com.raven.common.utils.Constants.CONFIG_INTERNAL_PORT;
import static com.raven.common.utils.Constants.CONFIG_TCP_PORT;
import static com.raven.common.utils.Constants.CONFIG_WEBSOCKET_PORT;
import static com.raven.common.utils.Constants.DEFAULT_SEPARATES_SIGN;
import static com.raven.common.utils.Constants.TOKEN_CACHE_DURATION;

import com.raven.common.enums.AccessServerType;
import com.raven.common.exception.TokenException;
import com.raven.common.loadbalance.ConsistentHashLoadBalancer;
import com.raven.common.loadbalance.LoadBalancer;
import com.raven.common.loadbalance.AccessServerInfo;
import com.raven.common.param.ServerInfoOutParam;
import com.raven.common.param.TokenInfoOutParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.route.user.service.UserService;
import com.raven.route.user.bean.Token;
import com.raven.route.validator.TokenValidator;
import com.raven.route.validator.UserValidator;
import com.raven.storage.route.RouteManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private TokenValidator tokenValidator;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RouteManager routeManager;

    @Override
    public Result getToken(String uid, String appKey) {
        // check uid validation.
        if (!userValidator.validate(uid)) {
            return Result.failure(userValidator.errorCode());
        }
        //  uid:timestamp:appkey => DES(appSecret) => BASE64 => token
        try {
            String token = new Token(uid, appKey).getToken(appKey);
            // cache token to redis.
            String key = appKey + DEFAULT_SEPARATES_SIGN + uid;
            stringRedisTemplate.opsForValue().set(token, key, TOKEN_CACHE_DURATION, TimeUnit.DAYS);
            return Result.success(new TokenInfoOutParam(appKey, uid, token));
        } catch (TokenException e) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_CREATE_ERROR);
        }
    }

    @Override
    public Result getAccessInfo(String token, AccessServerType type) {
        if (!tokenValidator.validate(token)) {
            return Result.failure(tokenValidator.errorCode());
        }
        String tokenStr = stringRedisTemplate.opsForValue().get(token);
        String uid = tokenStr.split(DEFAULT_SEPARATES_SIGN)[1];
        AccessServerInfo server = routeManager.getServerByUid(uid);
        if (null != server) {
            if (type == AccessServerType.WEBSOCKET) {
                return Result.success(new ServerInfoOutParam(server.getIp(), server.getWsPort()));
            }
            if (type == AccessServerType.TCP) {
                return Result.success(new ServerInfoOutParam(server.getIp(), server.getTcpPort()));
            }
        } else {

            List<ServiceInstance> instances = discoveryClient.getInstances(CONFIG_ACCESS_SERVER_NAME);
            List<AccessServerInfo> servers = new ArrayList<>();
            for (ServiceInstance instance : instances) {
                int tcpPort = Integer.valueOf(instance.getMetadata().get(CONFIG_TCP_PORT));
                int wsPort = Integer.valueOf(instance.getMetadata().get(CONFIG_WEBSOCKET_PORT));
                int internalPort = Integer
                    .valueOf(instance.getMetadata().get(CONFIG_INTERNAL_PORT));
                AccessServerInfo serverInfo = new AccessServerInfo(instance.getHost(), tcpPort,
                    wsPort, internalPort);
                servers.add(serverInfo);
            }
            if (!CollectionUtils.isEmpty(servers)) {
                LoadBalancer lb = new ConsistentHashLoadBalancer();
                AccessServerInfo origin = lb.select(servers, uid);
                if (type == AccessServerType.WEBSOCKET) {
                    return Result
                        .success(new ServerInfoOutParam(origin.getIp(), origin.getWsPort()));
                }
                if (type == AccessServerType.TCP) {
                    return Result
                        .success(new ServerInfoOutParam(origin.getIp(), origin.getTcpPort()));
                }
            }

        }
        return Result.failure(ResultCode.COMMON_NO_ACCESS_ERROR);
    }

}
