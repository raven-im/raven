package com.raven.storeredis.message.processor;

import com.raven.common.model.MsgContent;
import com.raven.common.protos.Message;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.JsonHelper;
import com.raven.storage.conver.ConverManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import static com.raven.common.utils.Constants.PREFIX_MESSAGE_ID;

@Slf4j
@AllArgsConstructor
public class MessageProcessor implements Runnable {

    private String message;
    private RedisTemplate redis;

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(message, builder);
        UpDownMessage upDownMessage = builder.getUpDownMessage();
        Message.MessageContent msg = upDownMessage.getContent();
        MsgContent msgContent = MsgContent.builder()
                .id(msg.getId())
                .uid(msg.getUid())
                .type(msg.getType().getNumber())
                .content(msg.getContent())
                .time(msg.getTime())
                .build();
        String str = JsonHelper.toJsonString(msgContent);
        redis.boundZSetOps(PREFIX_MESSAGE_ID + upDownMessage.getConverId()).add(str, msgContent.getId());
    }
}
