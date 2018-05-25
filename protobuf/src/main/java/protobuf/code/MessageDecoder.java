package protobuf.code;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.analysis.ParseMap;


public class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();
        if (in.readableBytes() < 4) {
            logger.info("readableBytes length less than 4 bytes, ignored");
            in.resetReaderIndex();
            return;
        }
        int length = in.readInt();
        if (length < 0) {
            ctx.close();
            logger.error("message length less than 0, channel closed");
            return;
        }
        if (length > in.readableBytes() - 4) {
            //注意！编解码器加这种in.readInt()日志，在大并发的情况下很可能会抛数组越界异常！
            //logger.error("message received is incomplete,ptoNum:{}, length:{}, readable:{}", in.readInt(), length, in.readableBytes());
            in.resetReaderIndex();
            return;
        }
        int ptoNum = in.readInt();
        ByteBuf byteBuf = Unpooled.buffer(length);
        in.readBytes(byteBuf);
        try {
            /* 解密消息体
            ThreeDES des = ctx.channel().attr(ClientAttr.ENCRYPT).get();
            byte[] bareByte = des.decrypt(inByte);*/
            byte[] body = byteBuf.array();
            MessageLite msg = ParseMap.getMessage(ptoNum, body);
            out.add(msg);
            logger.info("GateServer Received Message: content length {}, ptoNum: {}", length,
                    ptoNum);
        } catch (Exception e) {
            logger.error("{},decode failed:{}",ctx.channel().remoteAddress(), e);
        }
    }
}
