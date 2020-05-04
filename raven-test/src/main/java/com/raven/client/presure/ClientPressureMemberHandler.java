package com.raven.client.presure;

import com.raven.common.protos.Message.*;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.raven.client.common.Utils.*;

@Slf4j
public class ClientPressureMemberHandler extends SimpleChannelInboundHandler<RavenMessage> {

    private ChannelHandlerContext messageConnectionCtx;

    private String uid;

    private String token;

    public ClientPressureMemberHandler(String uid, String token) {
        this.uid = uid;
        this.token = token;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        messageConnectionCtx = ctx;
        sendLogin(ctx, token);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {
        if (message.getType() == Type.LoginAck) {
            LoginAck loginAck = message.getLoginAck();
            if (loginAck.getCode() == Code.SUCCESS) {
                log.info("uid:{} login success", uid);
            }
            sendHeartBeat(ctx);
        } else if (message.getType() == Type.MessageAck) {
            MessageAck messageAck = message.getMessageAck();
            log.info("receive message ack:{}", JsonHelper.toJsonString(messageAck));
        } else if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upDownMessage = message.getUpDownMessage();
            logAvgTimeDiff(System.currentTimeMillis() - upDownMessage.getContent().getTime());
        } else if (message.getType() == Type.HeartBeat) {
            rspHeartBeat(ctx, message.getHeartBeat());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

    private synchronized void logAvgTimeDiff(long timeDiff) {
        if (timeDiff > ClientPressureMember.maxTimeDiff) {
            ClientPressureMember.maxTimeDiff = (int) timeDiff;
        }
        int count = ClientPressureMember.msgCount.incrementAndGet();
        long cc = ClientPressureMember.countTimeDiff.addAndGet(timeDiff);
        log.info("消息总数:{}, 总延迟:{}ms", count, cc);
        int avgTime = (int) (cc / count);
        log.info("消息最大延迟：{}ms,平均延迟:{}ms", ClientPressureMember.maxTimeDiff, avgTime);
    }

}
