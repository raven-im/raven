package com.tim.group.tcp.common;

import com.tim.common.utils.Constants;
import com.tim.common.utils.GsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class OfflineMsgService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void storeOfflineMsg(String uid, Object message, String msgId, Long time) {
        stringRedisTemplate.boundZSetOps(Constants.OFF_USER_MSG_KEY + uid)
            .add(msgId, time);
        stringRedisTemplate.boundHashOps(Constants.TIM_OFFLINE_MESSAGE)
            .put(msgId, GsonHelper.getGson().toJson(message));

    }

    public void storeOfflineNotify(String uid, Object message, String msgId, Long time) {
        stringRedisTemplate.boundZSetOps(Constants.OFF_USER_MSG_KEY + uid)
            .add(msgId, time);
        stringRedisTemplate.boundHashOps(Constants.TIM_OFFLINE_MESSAGE)
            .put(msgId, GsonHelper.getGson().toJson(message));
    }

    public void storeWaitAckMessage(String uid, Object message, String msgId, Long time) {
        stringRedisTemplate.boundHashOps(Constants.TIM_WAIT_ACK_MESSAGE)
            .put(msgId, GsonHelper.getGson().toJson(message));
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
