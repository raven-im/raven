package com.tim.common.code;

import com.google.protobuf.MessageLite;
import com.tim.common.utils.ParseMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

/**
 * 编码器
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<MessageLite> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf out)
        throws Exception {
        byte[] bytes = msg.toByteArray();// 将对象转换为byte
        int ptoNum = ParseMap.msg2ptoNum.get(msg.getClass());
        int length = bytes.length;
        ByteBuf buf = Unpooled.buffer(8 + length);
        buf.writeInt(length);
        buf.writeInt(ptoNum);
        buf.writeBytes(bytes);
        out.writeBytes(buf);
        log.info("Send Message, remoteAddress:{}, content:{}",
            ctx.channel().remoteAddress(), msg.toString(), ptoNum);

    }
}
