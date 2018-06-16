package cn.timmy.message.handler;

import cn.timmy.message.process.PrivateMessageProcessor;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import cn.timmy.proto.protos.MessageProto.UpStreamMessageProto;

/**
 * Author zxx
 * Description 私聊消息处理
 * 单聊聊消息handler Date Created on 2018/5/25
 */
public class PrivateMessageHandler extends SimpleChannelInboundHandler<MessageLite> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
        MessageLite messageLite) throws Exception {
        if (messageLite instanceof UpStreamMessageProto) {
            PrivateMessageProcessor.getInstance().process(messageLite, channelHandlerContext);
        }
    }
}
