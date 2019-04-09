package com.tim.common.netty;

import com.tim.common.loadbalance.Server;
import io.netty.channel.Channel;
import java.util.List;

public interface ClientChannelManager {

    void addServer2Channel(Server server, Channel channel);

    Channel getChannelByServer(Server server);

    Server getServerByChannel(Channel channel);

    void removeServer(Server server);

}
