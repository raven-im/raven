package com.tim.common.code;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.tim.common.utils.JsonHelper;
import com.tim.common.utils.ParseMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

/**
 * 解码器
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            in.markReaderIndex();
            if (in.readableBytes() < 4) {
                log.error("readableBytes length less than 4 bytes, ignored");
                in.resetReaderIndex();
                return;
            }
            int length = in.readInt();
            if (length < 0) {
                log.error("message length less than 0, ignored");
                in.resetReaderIndex();
                return;
            }
            int ptoNum = in.readInt();
            ByteBuf byteBuf = Unpooled.buffer(length);
            in.readBytes(byteBuf);
            byte[] body = byteBuf.array();
            MessageLite msg = ParseMap.getMessage(ptoNum, body);
            out.add(msg);
            log.info("received message msg:{}", msg.toString());
        } catch (Exception e) {
            log.error("{},decode failed:{}", ctx.channel().remoteAddress(), e);
        }
    }
}
