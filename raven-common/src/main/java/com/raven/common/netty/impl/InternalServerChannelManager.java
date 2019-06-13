package com.raven.common.netty.impl;


import com.raven.common.loadbalance.AccessServerInfo;
import com.raven.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalServerChannelManager implements ServerChannelManager {

    private final Map<AccessServerInfo, Channel> serverChannel = new HashMap<>();

    private final Map<Channel, AccessServerInfo> channelServer = new HashMap<>();

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public void addServer2Channel(AccessServerInfo server, Channel channel) {
        rwLock.writeLock().lock();
        try {
            serverChannel.put(server, channel);
            channelServer.put(channel, server);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Channel getChannelByServer(AccessServerInfo server) {
        rwLock.readLock().lock();
        try {
            return serverChannel.get(server);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public AccessServerInfo getServerByChannel(Channel channel) {
        rwLock.readLock().lock();
        try {
            return channelServer.get(channel);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void removeServer(AccessServerInfo server) {
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
