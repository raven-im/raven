package cn.timmy.client;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cn.timmy.proto.utils.ParseMap;

public class Utils {


    private static final Logger logger = LogManager.getLogger(Utils.class);

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
