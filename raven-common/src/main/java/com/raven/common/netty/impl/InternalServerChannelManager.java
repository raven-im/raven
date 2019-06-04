package com.raven.common.netty.impl;


import com.raven.common.loadbalance.AceessServerInfo;
import com.raven.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InternalServerChannelManager implements ServerChannelManager {

    private final Map<AceessServerInfo, Channel> serverChannel = new HashMap<>();

    private final Map<Channel, AceessServerInfo> channelServer = new HashMap<>();

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public void addServer2Channel(AceessServerInfo server, Channel channel) {
        rwLock.writeLock().lock();
        try {
            serverChannel.put(server, channel);
            channelServer.put(channel, server);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public Channel getChannelByServer(AceessServerInfo server) {
        rwLock.readLock().lock();
        try {
            return serverChannel.get(server);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public AceessServerInfo getServerByChannel(Channel channel) {
        rwLock.readLock().lock();
        try {
            return channelServer.get(channel);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void removeServer(AceessServerInfo server) {
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
