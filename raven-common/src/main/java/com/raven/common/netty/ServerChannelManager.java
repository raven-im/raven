package com.raven.common.netty;

import com.raven.common.loadbalance.AceessServerInfo;
import io.netty.channel.Channel;

public interface ServerChannelManager {

    void addServer2Channel(AceessServerInfo server, Channel channel);

    Channel getChannelByServer(AceessServerInfo server);

    AceessServerInfo getServerByChannel(Channel channel);

    void removeServer(AceessServerInfo server);

}
