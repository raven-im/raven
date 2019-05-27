package com.raven.common.netty.impl;


import com.raven.common.loadbalance.Server;
import com.raven.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalServerChannelManager implements ServerChannelManager {

    private final Map<Server, Channel> serverChannel = new HashMap<>();

    private final Map<Channel, Server> channelServer = new HashMap<>();

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

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
}
