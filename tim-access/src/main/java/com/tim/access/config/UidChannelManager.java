package com.tim.access.config;

import com.tim.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * channel管理器
 */
@Component
public final class UidChannelManager implements ServerChannelManager {

    private final Map<String, List<Channel>> uidChannels = new HashMap<>();
    private final Map<Channel, String> channelUid = new HashMap<>();
    // TODO 锁粒度优化
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public void addId2Channel(String id, Channel channel) {
        rwLock.writeLock().lock();
        try {
            channelUid.put(channel, id);
            if (!channelUid.containsKey(channel)) {
                List<Channel> channels = new ArrayList<>();
                channels.add(channel);
                uidChannels.put(id, channels);
                return;
            }
            uidChannels.get(id).add(channel);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public List<Channel> getChannelsById(String id) {
        rwLock.readLock().lock();
        try {
            if (uidChannels.containsKey(id)) {
                return uidChannels.get(id);
            }
            return new ArrayList<>();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public String getIdByChannel(Channel channel) {
        rwLock.readLock().lock();
        try {
            if (channelUid.containsKey(channel)) {
                return channelUid.get(channel);
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void removeChannel(Channel channel) {
        rwLock.writeLock().lock();
        try {
            if (!channelUid.containsKey(channel)) {
                return;
            }
            String uid = channelUid.get(channel);
            List<Channel> channels = uidChannels.get(uid);
            channels.remove(channel);
            channelUid.remove(channel);
        } finally {
            rwLock.writeLock().unlock();
        }


    }
}
