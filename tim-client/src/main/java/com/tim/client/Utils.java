package com.tim.client;

import com.google.protobuf.MessageLite;
import com.tim.common.utils.ParseMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

    public static ByteBuf pack2Client(MessageLite msg) {
        byte[] bytes = msg.toByteArray();
        int length = bytes.length;
        int ptoNum = ParseMap.getPtoNum(msg);
        ByteBuf buf = Unpooled.buffer(8 + length);
        buf.writeInt(length);
        buf.writeInt(ptoNum);
        buf.writeBytes(bytes);
        log.info("Send Message, msg:{}", msg.toString());
        return buf;
    }
}
