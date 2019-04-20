package com.tim.common.netty;

import io.netty.channel.Channel;
import java.util.List;

public interface IdChannelManager {

    void addId2Channel(String id, Channel channel);

    List<Channel> getChannelsById(String id);

    String getIdByChannel(Channel channel);

    void removeChannel(Channel channel);

    List<String> getAllIds();

    List<Channel> getAllChannels();
}
