package com.raven.common.netty;

import com.raven.common.loadbalance.AccessServerInfo;
import io.netty.channel.Channel;

public interface ServerChannelManager {

    void addServer2Channel(AccessServerInfo server, Channel channel);

    Channel getChannelByServer(AccessServerInfo server);

    AccessServerInfo getServerByChannel(Channel channel);

    void removeServer(AccessServerInfo server);

}
