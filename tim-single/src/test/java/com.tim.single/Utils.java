package com.tim.single;

import com.google.protobuf.MessageLite;
import com.tim.common.utils.ParseMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

    public static ByteBuf pack2Client(MessageLite msg) {
        byte[] bytes = msg.toByteArray();
        int length = bytes.length;
        int messageType = ParseMap.getMessageType(msg);
        ByteBuf buf = Unpooled.buffer(8 + length);
        buf.writeInt(length);
        buf.writeInt(messageType);
        buf.writeBytes(bytes);
        log.info("Send Message, msg:{}", msg.toString());
        return buf;
    }
}
