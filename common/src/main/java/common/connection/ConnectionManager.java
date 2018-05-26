package common.connection;

import io.netty.channel.Channel;
import java.util.List;

public interface ConnectionManager {

    Connection get(Channel channel);

    Connection removeAndClose(Channel channel);

    void add(Connection connection);

    int getConnNum();

    void destroy();

    List<Connection> getConnectionByUid(String uid);

    void addUid(String uid, Channel channel);
}
