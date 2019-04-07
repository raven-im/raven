package com.tim.access.config;

import static com.tim.common.utils.Constants.CONFIG_NETTY_PORT;

import com.tim.common.loadbalance.Server;
import com.tim.common.utils.SnowFlake;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Author zxx Description 配置 Date Created on 2018/6/12
 */
@Configuration
@Slf4j
public class CustomConfig {

    @Autowired
    private DiscoveryClient discovery;

    @Value("${discovery.single-server-name}")
    private String singleServerName;

    @Value("${discovery.group-server-name}")
    private String groupServerName;

    @Value("${node.data-center-id}")
    private int dataCenterId;

    @Value("${node.machine-id}")
    private int machineId;

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(
        RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean(name = "singleServerList")
    public List<Server> singleServerList() {
        discovery.getServices().forEach(x -> log.info("###############:" + x));
        List<ServiceInstance> serviceInstances = discovery.getInstances(singleServerName);
        List<Server> servers = serviceInstances.stream()
            .map((x) -> {
                int nettyPort = Integer.valueOf(x.getMetadata().get(CONFIG_NETTY_PORT));
                return new Server(x.getHost(), nettyPort);
            }).collect(Collectors.toList());
        return servers;
    }

    @Bean(name = "groupServerList")
    public List<Server> groupServerList() {
        List<ServiceInstance> serviceInstances = discovery.getInstances(groupServerName);
        List<Server> servers = serviceInstances.stream()
            .map((x) -> {
                int nettyPort = Integer.valueOf(x.getMetadata().get(CONFIG_NETTY_PORT));
                return new Server(x.getHost(), nettyPort);
            }).collect(Collectors.toList());
        return servers;
    }

    @Bean
    public SnowFlake snowFlake() {
        return new SnowFlake(dataCenterId, machineId);
    }

}
