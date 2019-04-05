package com.tim.common.netty;

import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;

/**
 * Author zxx
 * Description 消息处理接口
 * Date Created on 2018/6/3
 */
public interface BaseMessageProcessor {

    void process(MessageLite messageLite, ChannelHandlerContext context);

}
