package com.tim.single.tcp.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tim.common.utils.Constants;
import com.tim.common.utils.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class OfflineMsgService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void storeOfflineMsg(String uid, Object message, String msgId, Long time)
        throws JsonProcessingException {
        stringRedisTemplate.boundZSetOps(Constants.OFF_USER_MSG_KEY + uid)
            .add(msgId, time);
        stringRedisTemplate.boundHashOps(Constants.TIM_OFFLINE_MESSAGE)
            .put(msgId, JsonHelper.toJsonString(message));

    }

    public void storeOfflineNotify(String uid, Object message, String msgId, Long time)
        throws JsonProcessingException {
        stringRedisTemplate.boundZSetOps(Constants.OFF_USER_MSG_KEY + uid)
            .add(msgId, time);
        stringRedisTemplate.boundHashOps(Constants.TIM_OFFLINE_MESSAGE)
            .put(msgId, JsonHelper.toJsonString(message));
    }

    public void storeWaitAckMessage(String uid, Object message, String msgId, Long time)
        throws JsonProcessingException {
        stringRedisTemplate.boundHashOps(Constants.TIM_WAIT_ACK_MESSAGE)
            .put(msgId, JsonHelper.toJsonString(message));
    }

    public void deleteAckMessage(String uid, String msgId, Long time) {
        stringRedisTemplate.boundZSetOps(Constants.OFF_USER_MSG_KEY + uid)
            .remove(time);
        stringRedisTemplate.boundHashOps(Constants.TIM_OFFLINE_MESSAGE)
            .delete(msgId);
        stringRedisTemplate.boundHashOps(Constants.TIM_WAIT_ACK_MESSAGE)
            .delete(msgId);
    }

}
