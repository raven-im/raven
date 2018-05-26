package client;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.utils.ParseMap;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static ByteBuf pack2Client(MessageLite msg) {
        byte[] bytes = msg.toByteArray();
        int length = bytes.length;
        int ptoNum = ParseMap.getPtoNum(msg);
        ByteBuf buf = Unpooled.buffer(8 + length);
        buf.writeInt(length);
        buf.writeInt(ptoNum);
        buf.writeBytes(bytes);
        logger.info("Send Message, msg:{}", msg.toString());
        return buf;
    }
}
