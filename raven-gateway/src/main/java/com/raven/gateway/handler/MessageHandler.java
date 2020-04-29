package com.raven.gateway.handler;

import com.raven.common.dubbo.MessageService;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.*;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.SnowFlake;
import com.raven.common.utils.UidUtil;
import com.raven.storage.conver.ConverManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            if (upMessage.getConverType() == ConverType.SINGLE) {
                if (StringUtils.isNotBlank(upMessage.getConverId())) {
                    if (!converManager.isSingleConverIdValid(upMessage.getConverId())) {
                        log.error("illegal conversation id.");
                        sendFailAck(ctx, upMessage, Code.CONVER_ID_INVALID);
                        return;
                    } else {
                        convId = upMessage.getConverId();
                    }
                } else if (StringUtils.isNotBlank(upMessage.getTargetUid())) {
                    convId = converManager
                            .newSingleConverId(upMessage.getFromUid(), upMessage.getTargetUid());
                    upMessage = upMessage.toBuilder().setConverId(convId).build();
                } else {
                    log.error("conversation id and target uid all empty.");
                    sendFailAck(ctx, upMessage, Code.NO_TARGET);
                    return;
                }
            } else if (upMessage.getConverType() == ConverType.GROUP) {
                convId = upMessage.getConverId();
                if (StringUtils.isNotBlank(convId)) {
                    String groupId = converManager.getGroupIdByConverId(convId);
                    if (StringUtils.isEmpty(groupId)) {
                        log.error("illegal conversation id.");
                        sendFailAck(ctx, upMessage, Code.CONVER_ID_INVALID);
                    }
                    upMessage = upMessage.toBuilder().setGroupId(groupId).build();
                } else if (StringUtils.isNotBlank(upMessage.getGroupId())) {
                    convId = UidUtil.uuid24ByFactor(upMessage.getGroupId());
                    upMessage = upMessage.toBuilder().setConverId(convId).build();
                } else {
                    log.error("conversation id and group id all empty.");
                    sendFailAck(ctx, upMessage, Code.NO_TARGET);
                    return;
                }
            } else {
                log.error("illegal conversation type.");
                sendFailAck(ctx, upMessage, Code.CONVER_TYPE_INVALID);
                return;
            }
            long id = snowFlake.nextId();
            RavenMessage ravenMessage = buildRavenMessage(ctx, upMessage, id);
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
                .setConverId(message.getConverId())
                .setTargetUid(message.getFromUid())
                .setCid(message.getCid())
                .setCode(code)
                .setTime(System.currentTimeMillis())
                .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.MessageAck)
                .setMessageAck(messageAck).build();
        ctx.writeAndFlush(ravenMessage);
    }

    private void sendFailAck(ChannelHandlerContext ctx, UpDownMessage message, Code code) {
        sendAck(ctx, message, code, 0);
    }

    private RavenMessage buildRavenMessage(ChannelHandlerContext ctx, UpDownMessage upDownMessage,
                                           long msgId) {
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        MessageContent content = MessageContent.newBuilder()
                .setId(msgId)
                .setType(upDownMessage.getContent().getType())
                .setUid(uid)
                .setContent(upDownMessage.getContent().getContent())
                .setTime(System.currentTimeMillis()).build();
        UpDownMessage upMessage = UpDownMessage.newBuilder().setId(msgId)
                .setCid(upDownMessage.getCid())
                .setFromUid(uid)
                .setTargetUid(upDownMessage.getTargetUid())
                .setContent(content)
                .setConverId(upDownMessage.getConverId())
                .setConverType(upDownMessage.getConverType())
                .setGroupId(upDownMessage.getGroupId())
                .build();
        return RavenMessage.newBuilder().setType(Type.UpDownMessage)
                .setUpDownMessage(upMessage).build();
    }

    private boolean isMsgClientIdValid(ChannelHandlerContext ctx, UpDownMessage upMessage) {
        if (0 == upMessage.getCid()) {
            return false;
        }
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        return !converManager.isUserCidExist(uid, upMessage.getCid());
    }

    private void saveUserClientId(ChannelHandlerContext ctx, UpDownMessage upMessage) {
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        converManager.saveUserCid(uid, upMessage.getCid());
    }

    private boolean sendMsg(RavenMessage ravenMessage, ConverType type, long id) {
        log.info("send Msg to {} : {}", id, type.getNumber());
        String message = JsonHelper.toJsonString(ravenMessage);
        if (StringUtils.isEmpty(message)) {
            return false;
        }
        log.debug("protobuf to json message:{}", message);
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
