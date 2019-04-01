package com.tim.group.common;

import io.netty.channel.Channel;
import java.util.List;

public interface ChannelManager {

    void addUid2Channel(String uid, Channel channel);

    List<Channel> getChannelByUid(String uid);

    String getUidByChannel(Channel channel);

    void removeChannel(Channel channel);
}
