package com.raven.admin.gateway.service.impl;

import com.raven.admin.gateway.bean.Token;
import com.raven.admin.gateway.service.GatewayService;
import com.raven.common.enums.GatewayServerType;
import com.raven.common.exception.TokenException;
import com.raven.common.loadbalance.ConsistentHashLoadBalancer;
import com.raven.common.loadbalance.GatewayServerInfo;
import com.raven.common.loadbalance.LoadBalancer;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.param.OutTokenInfoParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.raven.common.utils.Constants.*;

@Service
@Slf4j
public class GatewayServiceImpl implements GatewayService {

    @Value("${spring.cloud.zookeeper.gateway-server-path}")
    private String gatewayServerPath;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CuratorFramework curator;

    private List<GatewayServerInfo> gatewayServers = new ArrayList();

    @PostConstruct
    public void startClient() throws Exception {
        startZkWatcher();
    }

    @Override
    public Result getToken(String uid, String appKey) {
        try {
            String token = new Token(uid, appKey).getToken(appKey);
            String key = appKey + DEFAULT_SEPARATES_SIGN + uid;
            stringRedisTemplate.opsForValue().set(token, key, TOKEN_CACHE_DURATION, TimeUnit.DAYS);
            return Result.success(new OutTokenInfoParam(appKey, uid, token));
        } catch (TokenException e) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_CREATE_ERROR);
        }
    }

    @Override
    public Result getGatewaySite(String token, GatewayServerType type) {
        if (!stringRedisTemplate.hasKey(token)) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_INVALID);
        }
        String tokenStr = stringRedisTemplate.opsForValue().get(token);
        String uid = tokenStr.split(DEFAULT_SEPARATES_SIGN)[1];

        if (CollectionUtils.isEmpty(gatewayServers)) {
            List<ServiceInstance> instances = discoveryClient.getInstances(CONFIG_GATEWAY_SERVER_NAME);
            List<GatewayServerInfo> servers = new ArrayList<>();
            for (ServiceInstance instance : instances) {
                int tcpPort = Integer.parseInt(instance.getMetadata().get(CONFIG_TCP_PORT));
                int wsPort = Integer.parseInt(instance.getMetadata().get(CONFIG_WEBSOCKET_PORT));
                servers.add(new GatewayServerInfo(instance.getHost(), tcpPort, wsPort));
            }
            setGatewayServerList(servers);
        }
        if (CollectionUtils.isNotEmpty(gatewayServers)) {
            LoadBalancer lb = new ConsistentHashLoadBalancer();
            GatewayServerInfo origin = lb.select(gatewayServers, uid);
            if (type == GatewayServerType.WEBSOCKET) {
                return Result.success(new OutGatewaySiteInfoParam(origin.getIp(), origin.getWsPort()));
            }
            if (type == GatewayServerType.TCP) {
                return Result.success(new OutGatewaySiteInfoParam(origin.getIp(), origin.getTcpPort()));
            }
        }

        return Result.failure(ResultCode.COMMON_NO_GATEWAY_ERROR);
    }

    private void startZkWatcher() throws Exception {
        PathChildrenCache gatewayWatcher = new PathChildrenCache(curator, gatewayServerPath, true);
        gatewayWatcher.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curator,
                                   PathChildrenCacheEvent event) throws Exception {
                log.info("watched add gateway server node:{}",
                        new String(event.getData().getData()));
                clearGatewayServerList();
            }
        });
        gatewayWatcher.start();
        log.info("start watch gateway server");
    }

    private synchronized void clearGatewayServerList() {
        gatewayServers.clear();
    }

    private synchronized void setGatewayServerList(List<GatewayServerInfo> serverList) {
        gatewayServers.clear();
        gatewayServers.addAll(serverList);
    }

}
