package com.tim.single.tcp.manager;

import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ServerChannelManager;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: bbpatience
 * @date: 2019/4/10
 * @description: AccessServerManager
 **/
@Component
@Slf4j
public class AccessServerManager implements ServerChannelManager {

    private final Map<Server, Channel> serverChannel = new ConcurrentHashMap<>();
    private final Map<Channel, Server> channelServer = new ConcurrentHashMap<>();

    @Override
    public void addServer2Channel(Server server, Channel channel) {
        serverChannel.put(server, channel);
        channelServer.put(channel, server);
    }

    @Override
    public Channel getChannelByServer(Server server) {
        return serverChannel.get(server);
    }

    @Override
    public Server getServerByChannel(Channel channel) {
        return channelServer.get(channel);
    }

    @Override
    public void removeServer(Server server) {
        Channel channel = serverChannel.get(server);
        channelServer.remove(channel);
        serverChannel.remove(server);
    }
}
