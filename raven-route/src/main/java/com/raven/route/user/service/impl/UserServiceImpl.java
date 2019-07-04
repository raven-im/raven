package com.raven.route.user.service.impl;

import static com.raven.common.utils.Constants.CONFIG_ACCESS_SERVER_NAME;
import static com.raven.common.utils.Constants.CONFIG_INTERNAL_PORT;
import static com.raven.common.utils.Constants.CONFIG_TCP_PORT;
import static com.raven.common.utils.Constants.CONFIG_WEBSOCKET_PORT;
import static com.raven.common.utils.Constants.DEFAULT_SEPARATES_SIGN;
import static com.raven.common.utils.Constants.TOKEN_CACHE_DURATION;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.raven.common.enums.AccessServerType;
import com.raven.common.exception.TokenException;
import com.raven.common.loadbalance.AccessServerInfo;
import com.raven.common.loadbalance.ConsistentHashLoadBalancer;
import com.raven.common.loadbalance.LoadBalancer;
import com.raven.common.param.ServerInfoOutParam;
import com.raven.common.param.TokenInfoOutParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.route.user.bean.Token;
import com.raven.route.user.service.UserService;
import com.raven.route.validator.TokenValidator;
import com.raven.route.validator.UserValidator;
import com.raven.storage.route.RouteManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${discovery.access-server-path}")
    private String accessServerPath;

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

    @Autowired
    private CuratorFramework curator;

    private List<AccessServerInfo> accessServers = new ArrayList();

    @PostConstruct
    public void startClient() throws Exception {
        startZkWatcher();
    }

    @Override
    public Result getToken(String uid, String appKey) {
        if (!userValidator.validate(uid)) {
            return Result.failure(userValidator.errorCode());
        }
        try {
            String token = new Token(uid, appKey).getToken(appKey);
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
            if (CollectionUtils.isEmpty(accessServers)) {
                List<ServiceInstance> instances = discoveryClient
                    .getInstances(CONFIG_ACCESS_SERVER_NAME);
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
                setAccessServerList(servers);
            }
            if (CollectionUtils.isNotEmpty(accessServers)) {
                LoadBalancer lb = new ConsistentHashLoadBalancer();
                AccessServerInfo origin = lb.select(accessServers, uid);
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

    private void startZkWatcher() throws Exception {
        PathChildrenCache accessWatcher = new PathChildrenCache(curator, accessServerPath, true);
        accessWatcher.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curator,
                PathChildrenCacheEvent event) throws Exception {
                log.info("zookeeper watched add access server node:{}",
                    new String(event.getData().getData()));
                clearAccessServerList();
            }
        });
        accessWatcher.start();
        log.info("start access server zk watcher");
    }

    private synchronized void clearAccessServerList() {
        accessServers.clear();
    }

    private synchronized void setAccessServerList(List<AccessServerInfo> serverList) {
        accessServers.clear();
        accessServers.addAll(serverList);
    }

}
