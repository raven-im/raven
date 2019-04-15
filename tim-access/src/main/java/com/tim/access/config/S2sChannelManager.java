package com.tim.access.config;


import com.tim.common.loadbalance.ConsistentHashLoadBalancer;
import com.tim.common.loadbalance.LoadBalancer;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class S2sChannelManager implements ServerChannelManager {

    @Value("${discovery.single-server-name}")
    private String singleServerName;

    @Value("${discovery.group-server-name}")
    private String groupServerName;

    Set<Server> singleServerSet = new HashSet<>();

    Set<Server> groupServerSet = new HashSet<>();

    private final Map<Server, Channel> serverChannel = new HashMap<>();

    private final Map<Channel, Server> channelServer = new HashMap<>();

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void addSingleServer(Server server,Channel channel) {
        rwLock.writeLock().lock();
        try {
            singleServerSet.add(server);
            addServer2Channel(server,channel);
            log.info("add single server: " + server);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void addGroupServer(Server server,Channel channel) {
        rwLock.writeLock().lock();
        try {
            groupServerSet.add(server);
            addServer2Channel(server,channel);
            log.info("add group server: " + server);
        } finally {
            rwLock.writeLock().unlock();
        }
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
            if (singleServerSet.contains(server)) {
                singleServerSet.remove(server);
            } else {
                groupServerSet.remove(server);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public Channel selectGroupChannel(String conversationId) {
        LoadBalancer lb = new ConsistentHashLoadBalancer();
        Server server = lb.select(new ArrayList<Server>(groupServerSet), conversationId);
        return getChannelByServer(server);
    }

    public Channel selectSingleChannel(String conversationId) {
        LoadBalancer lb = new ConsistentHashLoadBalancer();
        Server server = lb.select(new ArrayList<Server>(singleServerSet), conversationId);
        return getChannelByServer(server);
    }
}
