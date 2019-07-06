package com.raven.common.netty;

import com.raven.common.loadbalance.GatewayServerInfo;
import io.netty.channel.Channel;

public interface ServerChannelManager {

    void addServer2Channel(GatewayServerInfo server, Channel channel);

    Channel getChannelByServer(GatewayServerInfo server);

    GatewayServerInfo getServerByChannel(Channel channel);

    void removeServer(GatewayServerInfo server);

}
