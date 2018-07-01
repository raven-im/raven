package cn.timmy.message.common;

import cn.timmy.common.utils.Constants;
import cn.timmy.common.utils.GsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/7/1
 */
@Component
public class OfflineMsgService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void storeOfflineMsg(String uid, Object message, Long msgId) {
        stringRedisTemplate.boundZSetOps(Constants.OFF_MSG_KEY + uid)
            .add(GsonHelper.getGson().toJson(message), msgId);
    }

    public void storeOfflineNotify(String uid, Object message, Long msgId) {
        stringRedisTemplate.boundZSetOps(Constants.OFF_NOTIFY_KEY + uid)
            .add(GsonHelper.getGson().toJson(message), msgId);
    }

}
