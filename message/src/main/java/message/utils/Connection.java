package message.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;


public interface Connection {

    byte STATUS_NEW = 0;
    byte STATUS_CONNECTED = 1;
    byte STATUS_DISCONNECTED = 2;

    void init(Channel channel);

    String getId();

    ChannelFuture close();

    Channel getChannel();

}
