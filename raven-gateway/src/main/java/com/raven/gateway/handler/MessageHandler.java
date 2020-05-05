package com.raven.gateway.handler;

import com.raven.common.dubbo.MessageService;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.netty.NettyAttrUtil;
import com.raven.common.protos.Message.*;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.SnowFlake;
import com.raven.storage.conver.ConverManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.raven.common.netty.NettyAttrUtil.ATTR_KEY_APP_KEY;

@Component
@Sharable
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private MessageService msgService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {

        if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upMessage = message.getUpDownMessage();
            if (!isMsgClientIdValid(ctx, upMessage)) {
                log.error(" client msg id repeat:{}", upMessage.getCid());
                sendFailAck(ctx, upMessage, Code.CLIENT_ID_REPEAT);
                return;
            } else {
                saveUserClientId(ctx, upMessage);
            }
            String convId;
            String appKey = NettyAttrUtil.getAttribute(ctx.channel(), ATTR_KEY_APP_KEY);
            if (upMessage.getConverType() == ConverType.SINGLE) {
                if (!StringUtils.isEmpty(upMessage.getConvId())) {
                    if (!converManager.isSingleConverIdValid(upMessage.getConvId())) {
                        log.error("illegal conversation id.");
                        sendFailAck(ctx, upMessage, Code.CONVER_ID_INVALID);
                        return;
                    } else {
                        convId = upMessage.getConvId();
                    }
                } else if (upMessage.getTargetUidCount() == 1) {
                    //single chat , only support 1 target uid.
                    convId = converManager.newSingleConverId(appKey, upMessage.getFromUid(), upMessage.getTargetUid(0));
                    upMessage = upMessage.toBuilder().setConvId(convId).build();
                } else {
                    log.error("No target!!");
                    sendFailAck(ctx, upMessage, Code.NO_TARGET);
                    return;
                }
            } else if (upMessage.getConverType() == ConverType.GROUP) {
                convId = upMessage.getConvId();
                if (StringUtils.isEmpty(convId)) {
                    log.error("no conversation id.");
                    sendFailAck(ctx, upMessage, Code.NO_TARGET);
                    return;
                } else if (!converManager.isGroupConverIdValid(convId)) {
                    log.error("illegal conversation id.");
                    sendFailAck(ctx, upMessage, Code.CONVER_ID_INVALID);
                    return;
                }
            } else {
                log.error("illegal conversation type.");
                sendFailAck(ctx, upMessage, Code.CONVER_TYPE_INVALID);
                return;
            }
            long id = snowFlake.nextId();
            RavenMessage ravenMessage = buildRavenMessage(ctx, upMessage, id, appKey);
            if (sendMsg(ravenMessage, upMessage.getConverType(), id)) {
                sendAck(ctx, upMessage, Code.SUCCESS, id);
            } else {
                log.error("send msg fail");
                sendFailAck(ctx, upMessage, Code.NO_TARGET);
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }

    private void sendAck(ChannelHandlerContext ctx, UpDownMessage message, Code code, long msgId) {
        MessageAck messageAck = MessageAck.newBuilder()
                .setId(msgId)
                .setCid(message.getCid())
                .setConvId(message.getConvId())
                .setCode(code)
                .setTime(System.currentTimeMillis())
                .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder()
                .setType(Type.MessageAck)
                .setMessageAck(messageAck)
                .build();
        ctx.writeAndFlush(ravenMessage);
    }

    private void sendFailAck(ChannelHandlerContext ctx, UpDownMessage message, Code code) {
        sendAck(ctx, message, code, 0);
    }

    private RavenMessage buildRavenMessage(ChannelHandlerContext ctx, UpDownMessage upDownMessage, long msgId, String appKey) {
        MessageContent content = MessageContent.newBuilder()
                .setType(upDownMessage.getContent().getType())
                .setContent(upDownMessage.getContent().getContent())
                .setTime(System.currentTimeMillis())
                .build();
        SSMessage message = SSMessage.newBuilder()
                .setId(msgId)
                .setFromUid(upDownMessage.getFromUid())
                .addAllTargetUid(upDownMessage.getTargetUidList())
                .setConvId(upDownMessage.getConvId())
                .setAppKey(appKey)
                .setConverType(upDownMessage.getConverType())
                .setContent(content)
                .build();
        return RavenMessage.newBuilder()
                .setType(Type.SSMessage)
                .setSsMessage(message)
                .build();
    }

    private boolean isMsgClientIdValid(ChannelHandlerContext ctx, UpDownMessage upMessage) {
        if (0 == upMessage.getCid()) {
            return false;
        }
        String uid = uidChannelManager.getUidByChannel(ctx.channel());
        return !converManager.isUserCidExist(uid, upMessage.getCid());
    }

    /**
     * 为保证cid相同的客户端消息不重复发送，缓存该消息cid一段时间，
     *
     * @param ctx
     * @param upMessage
     */
    private void saveUserClientId(ChannelHandlerContext ctx, UpDownMessage upMessage) {
        String uid = uidChannelManager.getUidByChannel(ctx.channel());
        converManager.saveUserCid(uid, upMessage.getCid());
    }

    private boolean sendMsg(RavenMessage ravenMessage, ConverType type, long id) {
//        log.info("send Msg to {} : {}", id, type.getNumber());
        String message = JsonHelper.toJsonString(ravenMessage);
        log.info("send Msg to {} : {}", id, message);
        if (StringUtils.isEmpty(message)) {
            return false;
        }
        log.debug("protobuf to json message:{}", message);
        //TODO  dubbo not support PB Serialize.    Need to find other solution?
        if (type == ConverType.SINGLE) {
            msgService.singleMsgSend(message);
        } else if (type == ConverType.GROUP) {
            msgService.groupMsgSend(message);
        } else {
            log.error("type error, message not sent:{}", message);
            return false;
        }
        return true;
    }
}
