package message.utils;

import io.netty.channel.Channel;
import java.util.List;

public interface ConnectionManager {

    Connection get(Channel channel);

    Connection removeAndClose(Channel channel);

    void add(Connection connection);

    int getConnNum();

    void init();

    void destroy();

    List getConnectionByUid(String uid);

    void addUid(String uid, Channel channel);
}
