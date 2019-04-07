package com.tim.access.config;

import com.tim.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * channel管理器
 */
@Component
public final class UidChannelManager implements ServerChannelManager {

    private final ConcurrentMap<String, List<Channel>> uidChannels = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, String> channelUid = new ConcurrentHashMap<>();

    @Override
    public void addId2Channel(String id, Channel channel) {
        channelUid.put(channel, id);
        if (!channelUid.containsKey(channel)) {
            List<Channel> channels = new ArrayList<>();
            channels.add(channel);
            uidChannels.put(id, channels);
            return;
        }
        uidChannels.get(id).add(channel);
    }

    @Override
    public List<Channel> getChannelsById(String id) {
        if (uidChannels.containsKey(id)) {
            return uidChannels.get(id);
        }
        return new ArrayList<>();
    }

    @Override
    public String getIdByChannel(Channel channel) {
        if (channelUid.containsKey(channel)) {
            return channelUid.get(channel);
        }
        return null;
    }

    @Override
    public void removeChannel(Channel channel) {
        if (!channelUid.containsKey(channel)) {
            return;
        }
        String uid = channelUid.get(channel);
        List<Channel> channels = uidChannels.get(uid);
        channels.remove(channel);
        channelUid.remove(channel);
    }
}
