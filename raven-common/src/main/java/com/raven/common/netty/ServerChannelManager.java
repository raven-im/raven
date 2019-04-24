package com.raven.common.netty;

import com.raven.common.loadbalance.Server;
import io.netty.channel.Channel;

public interface ServerChannelManager {

    void addServer2Channel(Server server, Channel channel);

    Channel getChannelByServer(Server server);

    Server getServerByChannel(Channel channel);

    void removeServer(Server server);

}
