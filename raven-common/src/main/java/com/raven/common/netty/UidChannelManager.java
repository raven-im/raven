package com.raven.common.netty;

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class UidChannelManager implements IdChannelManager {

    private final Map<String, List<Channel>> uidChannels = new ConcurrentHashMap<>();

    @Override
    public void addId2Channel(String id, Channel channel) {
        NettyAttrUtil.setAttrKeyUserId(channel, id);
        if (!uidChannels.containsKey(id)) {
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
        return NettyAttrUtil.getUid(channel);
    }

    @Override
    public void removeChannel(Channel channel) {
        String uid = NettyAttrUtil.getUid(channel);
        if (null != uid) {
            uidChannels.remove(uid);
        }
    }

    @Override
    public List<String> getAllIds() {
        List<String> idList = new ArrayList<>();
        for (Entry<String, List<Channel>> entry : uidChannels.entrySet()) {
            idList.add(entry.getKey());
        }
        return idList;
    }

    @Override
    public List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<>();
        for (Entry<String, List<Channel>> entry : uidChannels.entrySet()) {
            channels.addAll(entry.getValue());
        }
        return channels;
    }
}
