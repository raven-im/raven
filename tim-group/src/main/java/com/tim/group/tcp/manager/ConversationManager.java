package com.tim.group.tcp.manager;

import static com.tim.common.utils.Constants.PREFIX_CONVERSATION_ID;
import static com.tim.common.utils.Constants.PREFIX_CONVERSATION_LIST;
import static com.tim.common.utils.Constants.PREFIX_MESSAGE_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.common.utils.JsonHelper;
import com.tim.common.utils.UidUtil;
import com.tim.group.tcp.model.ConversationModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author: bbpatience
 * @date: 2019/4/9
 * @description: ConversationManager
 **/
@Slf4j
@Component
public class ConversationManager {

    @Autowired
    private RedisTemplate redisTemplate;

    public void cacheConversation(UpDownMessage msg, String convId) {
        //1. cache redis "convid_" + convId,  1 to 1 , ConversationModel
        //TODO  conversation name ??
        ConversationModel model = new ConversationModel(convId, ConversationType.GROUP, "Group",
            msg.getContent().getTime(), msg.getContent().getContent());
        try {
            String modelStr = JsonHelper.toJsonString(model);
            redisTemplate.boundHashOps(PREFIX_CONVERSATION_ID + convId).put(PREFIX_CONVERSATION_ID + convId, modelStr);
        } catch (JsonProcessingException e) {
            log.error("Json processing error.");
            return;
        }

        //2. cache redis "msg_" + convId, 1 to set, UpDownMessage
        redisTemplate.boundSetOps(PREFIX_MESSAGE_ID + convId).add(msg);
        //3. cache redis "convlist_" + uid(fromId, targetId), 1 to set, convId
        redisTemplate.boundSetOps(PREFIX_CONVERSATION_LIST + msg.getFromId()).add(convId);
        redisTemplate.boundSetOps(PREFIX_CONVERSATION_LIST + msg.getTargetId()).add(convId);
    }
}
