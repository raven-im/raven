package protobuf.code;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.utils.ParseMap;

/**
 * 编码器
 */
public class MessageEncoder extends MessageToByteEncoder<MessageLite> {

    private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);

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
        logger.info("Send Message, remoteAddress:{}, content:{}, ptoNum:{}",
            ctx.channel().remoteAddress(), msg.toString(), ptoNum);

    }
}
