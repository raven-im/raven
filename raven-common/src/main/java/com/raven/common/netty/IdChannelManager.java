package com.raven.common.netty;

import io.netty.channel.Channel;
import java.util.List;

public interface IdChannelManager {

    // uid =  AppKey + userId
    void addUid2Channel(String uid, Channel channel, String deviceId);

    List<Channel> getChannelsByUid(String uid);

    String getUidByChannel(Channel channel);

    void removeChannel(Channel channel);
//
//    List<String> getAllIds();
//
//    List<Channel> getAllChannels();
}
