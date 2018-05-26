package message.utils;

import common.connection.Connection;
import common.connection.ConnectionManager;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * netty连接管理器
 */
public final class NettyConnectionManager implements ConnectionManager {

    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<Connection>> uidConnections = new ConcurrentHashMap<>();

    private static NettyConnectionManager connectionManager;

    public static synchronized NettyConnectionManager getInstance() {
        if (connectionManager == null) {
            connectionManager = new NettyConnectionManager();
        }
        return connectionManager;
    }

    @Override
    public Connection getConnection(Channel channel) {
        return connections.get(channel.id().asShortText());
    }

    @Override
    public Connection removeAndClose(Channel channel) {
        return connections.remove(channel.id().asShortText());
    }

    @Override
    public void addConnection(Connection connection) {
        connections.putIfAbsent(connection.getChannel().id().asShortText(), connection);
    }

    @Override
    public int getConnNum() {
        return connections.size();
    }

    @Override
    public void destroy() {
        connections.values().forEach(Connection::close);
        connections.clear();
    }

    @Override
    public List<Connection> getConnectionByUid(String uid) {
        return uidConnections.get(uid);
    }

    @Override
    public void addUid2Connection(String uid, Channel channel) {
        if (uidConnections.get(uid) == null) {
            List<Connection> list = new ArrayList<>();
            list.add(getConnection(channel).setUid(uid));
            uidConnections.put(uid, list);
            return;
        }
        List<Connection> list = uidConnections.get(uid);
        list.add(getConnection(channel).setUid(uid));
    }

    private NettyConnectionManager() {
    }
}
