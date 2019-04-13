package com.tim.storage.conver;

import static com.tim.common.utils.Constants.PREFIX_CONVERSATION_LIST;
import static com.tim.common.utils.Constants.PREFIX_MESSAGE_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tim.common.model.ConverInfo;
import com.tim.common.model.MsgContent;
import com.tim.common.protos.Common.ConverType;
import com.tim.common.protos.Common.MessageContent;
import com.tim.common.utils.JsonHelper;
import com.tim.common.utils.UidUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

@Slf4j
public class ConverManager {

    private RedisTemplate redisTemplate;

    public ConverManager(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private ConverManager() {
    }

    public String newSingleConverId(String fromUid, String toUid) {
        String converId = UidUtil.uuid24By2Factor(fromUid, toUid);
        Set<String> uidList = new HashSet<>();
        uidList.add(fromUid);
        uidList.add(toUid);
        ConverInfo converInfo = new ConverInfo().setId(converId).setType(ConverType.SINGLE.getNumber())
            .setUidList(CollectionUtils.arrayToList(uidList.toArray()));
        try {
            if (redisTemplate.opsForValue()
                .setIfAbsent(converId, JsonHelper.toJsonString(converInfo))) {
                redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + fromUid).put(converId, 0);
                redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + toUid).put(converId, 0);
            }
        } catch (JsonProcessingException e) {
            log.error("json processing error", e);
        }
        return converId;
    }

    public boolean isSingleConverIdValid(String converId) {
        ConverInfo converInfo = getConverInfo(converId);
        return converInfo == null ? false : converInfo.getType() == ConverType.SINGLE.getNumber();
    }

    public void cacheMsg2Conver(MessageContent msg, String converId)
        throws Exception {
        MsgContent msgContent = new MsgContent().setId(msg.getId()).setUid(msg.getUid())
            .setType(msg.getType().getNumber()).setContent(msg.getContent()).setTime(msg.getTime());
        String str = JsonHelper.toJsonString(msgContent);
        redisTemplate.boundZSetOps(PREFIX_MESSAGE_ID + converId).add(str, msgContent.getTime());

    }

    public ConverInfo getConverInfo(String converId) {
        Object ob = redisTemplate.opsForValue().get(converId);
        if (null == ob) {
            return null;
        }
        return JsonHelper.readValue(ob.toString(), ConverInfo.class);
    }

    public List<String> getUidListByConver(String converId) {
        ConverInfo converInfo = getConverInfo(converId);
        if (converInfo.getType() == ConverType.SINGLE.getNumber()) {
            return converInfo.getUidList();
        } else if (converInfo.getType() == ConverType.GROUP.getNumber()) {
            // TODO 从缓存拿群成员ID
        }
        return new ArrayList<>();
    }

    public void incrUserConverUnCount(String uid, String converId, int num) {
        redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + uid).increment(converId, num);
    }

}
