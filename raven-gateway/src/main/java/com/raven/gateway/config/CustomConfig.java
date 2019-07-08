package com.raven.gateway.config;

import com.raven.common.netty.IdChannelManager;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.netty.impl.InternalServerChannelManager;
import com.raven.common.netty.impl.UidChannelManager;
import com.raven.common.utils.SnowFlake;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Author zxx Description 配置 Date Created on 2018/6/12
 */
@Configuration
@Slf4j
public class CustomConfig {

    @Value("${node.data-center-id}")
    private int dataCenterId;

    @Value("${node.machine-id}")
    private int machineId;

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(
        RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        RedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(serializer);
        template.setDefaultSerializer(serializer);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public SnowFlake snowFlake() {
        return new SnowFlake(dataCenterId, machineId);
    }

    @Bean
    public IdChannelManager uidChannelManager() {
        return new UidChannelManager();
    }

    @Bean
    public ServerChannelManager internalServerChannelManager() {
        return new InternalServerChannelManager();
    }

    @Bean
    @DependsOn("redisTemplate")
    public ConverManager conversationManager(RedisTemplate redisTemplate) {
        return new ConverManager(redisTemplate);
    }

    @Bean
    @DependsOn("redisTemplate")
    public RouteManager routeManager(RedisTemplate redisTemplate) {
        return new RouteManager(redisTemplate);
    }

}
