package common.connection;

import io.netty.channel.Channel;
import java.util.List;

public interface ConnectionManager {

    Connection getConnection(Channel channel);

    Connection removeAndClose(Channel channel);

    void addConnection(Connection connection);

    int getConnNum();

    void destroy();

    List<Connection> getConnectionByUid(String uid);

    void addUid2Connection(String uid, Channel channel);
}
