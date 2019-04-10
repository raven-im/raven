package com.tim.access.config;


import static com.tim.common.utils.Constants.CONFIG_NETTY_PORT;

import com.tim.common.loadbalance.ConsistentHashLoadBalancer;
import com.tim.common.loadbalance.LoadBalancer;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class S2sChannelManager implements ServerChannelManager {

    @Autowired
    private DiscoveryClient discovery;

    @Value("${discovery.single-server-name}")
    private String singleServerName;

    @Value("${discovery.group-server-name}")
    private String groupServerName;

    List<Server> singleServerList = new ArrayList<>();

    List<Server> groupServerList = new ArrayList<>();

    private final Map<Server, Channel> serverChannel = new HashMap<>();

    private final Map<Channel, Server> channelServer = new HashMap<>();

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void addSingleServer(Server server) {
        rwLock.writeLock().lock();
        try {
            singleServerList.add(server);
            log.info("add single server: " + server);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void addGroupServer(Server server) {
        rwLock.writeLock().lock();
        try {
            groupServerList.add(server);
            log.info("add group server: " + server);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public List<Server> getGroupServerList() {
        return groupServerList;
    }

    public List<Server> getSingleServerList() {
        return singleServerList;
    }

    @PostConstruct
    public void init() {
        addSingleServer();
        addGroupServer();
    }

    private void addSingleServer() {
        List<ServiceInstance> serviceInstances = discovery.getInstances(singleServerName);
        serviceInstances.forEach(x -> {
            int nettyPort = Integer.valueOf(x.getMetadata().get(CONFIG_NETTY_PORT));
            addSingleServer(new Server(x.getHost(), nettyPort));
        });
    }

    private void addGroupServer() {
        List<ServiceInstance> serviceInstances = discovery.getInstances(groupServerName);
        serviceInstances.forEach(x -> {
            int nettyPort = Integer.valueOf(x.getMetadata().get(CONFIG_NETTY_PORT));
            addGroupServer(new Server(x.getHost(), nettyPort));
        });
    }

    @Override
    public void addServer2Channel(Server server, Channel channel) {
        rwLock.writeLock().lock();
        try {
            serverChannel.put(server, channel);
            channelServer.put(channel, server);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Channel getChannelByServer(Server server) {
        rwLock.readLock().lock();
        try {
            return serverChannel.get(server);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Server getServerByChannel(Channel channel) {
        rwLock.readLock().lock();
        try {
            return channelServer.get(channel);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void removeServer(Server server) {
        rwLock.writeLock().lock();
        try {
            Channel channel = serverChannel.get(server);
            channelServer.remove(channel);
            serverChannel.remove(server);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Channel selectGroupChannel(String conversationId) {
        LoadBalancer lb = new ConsistentHashLoadBalancer();
        Server server = lb.select(groupServerList, conversationId);
        return getChannelByServer(server);
    }

    public Channel selectSingleChannel(String conversationId) {
        LoadBalancer lb = new ConsistentHashLoadBalancer();
        Server server = lb.select(singleServerList, conversationId);
        return getChannelByServer(server);
    }
}
