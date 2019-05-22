package com.raven.access.handler.server;

import com.googlecode.protobuf.format.JsonFormat;
import com.raven.access.config.KafkaProducerManager;
import com.raven.access.config.S2sChannelManager;
import com.raven.common.netty.IdChannelManager;
import com.raven.common.protos.Message.Code;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.MessageAck;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.common.utils.Constants;
import com.raven.common.utils.SnowFlake;
import com.raven.storage.conver.ConverManager;
import io.netty.channel.Channel;
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
public class MesaageHandler extends SimpleChannelInboundHandler<RavenMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private S2sChannelManager s2sChannelManager;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private KafkaProducerManager kafkaProducerManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RavenMessage message) throws Exception {
        if (message.getType() == Type.UpDownMessage) {
            UpDownMessage upMessage = message.getUpDownMessage();
            log.info("receive up message:{}", upMessage);
            if (verifyMsgClientId(ctx, upMessage)) {
                sendFailAck(ctx, upMessage);
                return;
            } else {
                saveUserClientId(ctx, upMessage);
            }
            String topic;
            if (upMessage.getConverType() == ConverType.SINGLE) {
                if (StringUtils.isBlank(upMessage.getConverId()) && StringUtils
                    .isBlank(upMessage.getTargetUid())) {
                    sendFailAck(ctx, upMessage);
                    return;
                }
                topic = Constants.KAFKA_TOPIC_SINGLE_MSG;
            } else if (upMessage.getConverType() == ConverType.GROUP) {
                if (StringUtils.isBlank(upMessage.getConverId()) && StringUtils
                    .isBlank(upMessage.getGroupId())) {
                    sendFailAck(ctx, upMessage);
                    return;
                }
                topic = Constants.KAFKA_TOPIC_GROUP_MSG;
            } else {
                sendFailAck(ctx, upMessage);
                return;
            }
            RavenMessage ravenMessage = buildRavenMessage(ctx, upMessage);
            if (!sendMsgToKafka(ravenMessage, ravenMessage.getUpDownMessage().getId(), topic)) {
                sendFailAck(ctx, upMessage);
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }

    private void sendFailAck(ChannelHandlerContext ctx, UpDownMessage message) {
        MessageAck messageAck = MessageAck.newBuilder()
            .setCid(message.getCid())
            .setTargetUid(uidChannelManager.getIdByChannel(ctx.channel()))
            .setCode(Code.FAIL)
            .setTime(System.currentTimeMillis())
            .setConverId(message.getConverId())
            .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.MessageAck)
            .setMessageAck(messageAck).build();
        ctx.writeAndFlush(ravenMessage);
    }

    private RavenMessage buildRavenMessage(ChannelHandlerContext ctx, UpDownMessage upDownMessage) {
        long id = snowFlake.nextId();
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        MessageContent content = MessageContent.newBuilder().setId(id)
            .setType(upDownMessage.getContent().getType()).setUid(uid)
            .setContent(upDownMessage.getContent().getContent())
            .setTime(System.currentTimeMillis()).build();
        UpDownMessage upMesaage = UpDownMessage.newBuilder().setId(id)
            .setCid(upDownMessage.getCid()).setFromUid(uid)
            .setTargetUid(upDownMessage.getTargetUid()).setContent(content)
            .setConverId(upDownMessage.getConverId()).setConverType(upDownMessage.getConverType())
            .setGroupId(upDownMessage.getGroupId())
            .build();
        RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
            .setUpDownMessage(upMesaage).build();
        return ravenMessage;
    }

    private boolean verifyMsgClientId(ChannelHandlerContext ctx, UpDownMessage upMessage) {
        if (0 == upMessage.getCid()) {
            return true;
        }
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        return converManager.isClientIdExist(uid, upMessage.getCid());
    }

    private void saveUserClientId(ChannelHandlerContext ctx, UpDownMessage upMessage) {
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        converManager.saveUserCid(uid, upMessage.getCid());
    }

    private boolean sendMsgToKafka(RavenMessage ravenMessage, long id, String topic) {
        JsonFormat jsonFormat = new JsonFormat();
        String message = jsonFormat.printToString(ravenMessage);
        log.info("protobuf to json message:{}", message);
        Result result = kafkaProducerManager.send(topic, String.valueOf(id), message);
        return result.getCode().intValue() == ResultCode.COMMON_SUCCESS.getCode();
    }


}
