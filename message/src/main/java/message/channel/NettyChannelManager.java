package message.channel;

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import message.common.ChannelManager;

/**
 * netty连接管理器
 */
public final class NettyChannelManager implements ChannelManager {

    private final ConcurrentMap<String, List<Channel>> uidChannels = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, String> channelUid = new ConcurrentHashMap<>();

    private static NettyChannelManager connectionManager;

    public static synchronized NettyChannelManager getInstance() {
        if (connectionManager == null) {
            connectionManager = new NettyChannelManager();
        }
        return connectionManager;
    }

    private NettyChannelManager() {
    }

    @Override
    public void addUid2Channel(String uid, Channel channel) {
        channelUid.put(channel, uid);
        if (null == uidChannels.get(uid)) {
            List<Channel> channels = new ArrayList<>();
            channels.add(channel);
            uidChannels.put(uid, channels);
            return;
        }
        uidChannels.get(uid).add(channel);
    }

    @Override
    public List<Channel> getChannelByUid(String uid) {
        return uidChannels.get(uid);
    }

    @Override
    public String getUidByChannel(Channel channel) {
        return channelUid.get(channel);
    }

    @Override
    public void removeChannel(Channel channel) {
        String uid = channelUid.get(channel);
        List<Channel> channels = uidChannels.get(uid);
        channels.remove(channel);
        channelUid.remove(channel);
    }
}
