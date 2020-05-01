package com.raven.storeredis.message.processor;

import com.raven.common.model.NotifyContent;
import com.raven.common.protos.Message.NotifyMessage;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.utils.JsonHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import static com.raven.common.utils.Constants.PREFIX_MESSAGE_ID;

@Slf4j
@AllArgsConstructor
public class NotifyProcessor implements Runnable {
    private String notification;
    private RedisTemplate redis;

    @Override
    public void run() {
        RavenMessage.Builder builder = RavenMessage.newBuilder();
        JsonHelper.readValue(notification, builder);
        NotifyMessage notify = builder.getNotifyMessage();

        NotifyContent msgContent = NotifyContent.builder()
                .id(notify.getId())
                .type(notify.getType().getNumber())
                .content(notify.getContent())
                .time(notify.getTime())
                .build();
        String str = JsonHelper.toJsonString(msgContent);
        redis.boundZSetOps(PREFIX_MESSAGE_ID + notify.getConverId()).add(str, msgContent.getId());
    }
}
