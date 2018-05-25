package message.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public final class NettyConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(NettyConnection.class);
    private Channel channel;
    private volatile byte status = STATUS_NEW;
    private String uid;

    @Override
    public void init(Channel channel) {
        this.channel = channel;
        this.status = STATUS_CONNECTED;
    }


    @Override
    public String getId() {
        return channel.id().asShortText();
    }

    @Override
    public ChannelFuture close() {
        if (status == STATUS_DISCONNECTED) {
            return null;
        }
        this.status = STATUS_DISCONNECTED;
        return this.channel.close();
    }

    @Override
    public String toString() {
        return "[channel=" + channel
                + ", status=" + status
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

        return channel.id().equals(that.channel.id());
    }

    @Override
    public int hashCode() {
        return channel.id().hashCode();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
