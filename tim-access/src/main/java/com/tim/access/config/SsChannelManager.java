package com.tim.access.config;

import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ClientChannelManager;
import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class SsChannelManager implements ClientChannelManager {

    private final ConcurrentMap<Server, Channel> clientChannel = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, Server> channelClient = new ConcurrentHashMap<>();

    @Override
    public void addServer2Channel(Server server, Channel channel) {
        channelClient.put(channel, server);
        clientChannel.put(server, channel);
    }

    @Override
    public Channel getChannelByServer(Server server) {
        return clientChannel.get(server);
    }

    @Override
    public Server getServerByChannel(Channel channel) {
        return channelClient.get(channel);
    }

    @Override
    public void removeServer(Server server) {
        Channel channel = clientChannel.get(server);
        clientChannel.remove(server);
        channelClient.remove(channel);
    }
}
