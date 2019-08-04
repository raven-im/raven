package com.raven.common.netty;

import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.ConverAck;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import io.netty.channel.ChannelHandlerContext;

public class HandlerUtil {

    public static void sendConverFailAck(ChannelHandlerContext ctx, Long id, Code code) {
        ConverAck converAck = ConverAck.newBuilder()
            .setId(id)
            .setCode(code)
            .setTime(System.currentTimeMillis())
            .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder()
            .setType(Type.ConverAck)
            .setConverAck(converAck).build();
        ctx.writeAndFlush(ravenMessage);
    }
}
