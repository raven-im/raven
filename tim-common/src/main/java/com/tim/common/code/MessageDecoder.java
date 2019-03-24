package com.tim.common.code;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
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
        in.markReaderIndex();
        if (in.readableBytes() < 4) {
            log.info("readableBytes length less than 4 bytes, ignored");
            in.resetReaderIndex();
            return;
        }
        int length = in.readInt();
        if (length < 0) {
            ctx.close();
            log.error("message length less than 0, channel closed");
            return;
        }
        if (length > in.readableBytes() - 4) {
            in.resetReaderIndex();
            return;
        }
        int ptoNum = in.readInt();
        ByteBuf byteBuf = Unpooled.buffer(length);
        in.readBytes(byteBuf);
        try {
            byte[] body = byteBuf.array();
            MessageLite msg = ParseMap.getMessage(ptoNum, body);
            out.add(msg);
            log.info("Received Message remoteAddress:{}, content:{}",
                ctx.channel().remoteAddress(), msg.toString(),
                ptoNum);
        } catch (Exception e) {
            log.error("{},decode failed:{}", ctx.channel().remoteAddress(), e);
        }
    }
}
