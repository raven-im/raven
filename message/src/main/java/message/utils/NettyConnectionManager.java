package message.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ohun on 2016/12/27.
 *
 * @author ohun@live.cn (夜色)
 */
public final class NettyConnectionManager implements ConnectionManager {

    private final ConcurrentMap<ChannelId, Connection> connections = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<Connection>> uidConnections = new ConcurrentHashMap<>();


    @Override
    public Connection get(Channel channel) {
        return connections.get(channel.id());
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        return connections.remove(channel.id());
    }

    @Override
    public void add(Connection connection) {
        connections.putIfAbsent(connection.getChannel().id(), connection);
    }

    @Override
    public int getConnNum() {
        return connections.size();
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        connections.values().forEach(Connection::close);
        connections.clear();
    }

    @Override
    public List getConnectionByUid(String uid) {
        return uidConnections.get(uid);
    }

    @Override
    public void addUid(String uid, Channel channel) {
        if (uidConnections.get(uid) == null) {
            List<Connection> list = new ArrayList<>();
            list.add(get(channel));
            uidConnections.put(uid, list);
        }
        List<Connection> list = uidConnections.get(uid);
        list.add(get(channel));
    }
}
