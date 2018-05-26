package message.utils;

import common.connection.Connection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty连接封装
 */
public final class NettyConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(NettyConnection.class);
    private Channel channel;
    private String uid;

    @Override
    public void init(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String getId() {
        return channel.id().asShortText();
    }

    @Override
    public ChannelFuture close() {
        return this.channel.close();
    }

    @Override
    public String toString() {
        return "[channel=" + channel.id().asShortText()
                + "]";
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NettyConnection that = (NettyConnection) o;
        return channel.id().asShortText().equals(that.channel.id().asShortText());
    }

    @Override
    public int hashCode() {
        return channel.id().asShortText().hashCode();
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public Connection setUid(String uid) {
        this.uid = uid;
        return this;
    }
}
