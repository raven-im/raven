package common.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * 连接抽象
 */
public interface Connection {

    String getUid();

    void init(Channel channel);

    String getId();

    Connection setUid(String uid);

    ChannelFuture close();

    Channel getChannel();

}
