package com.raven.storage.conver;

import com.google.common.collect.Lists;
import com.raven.common.model.Conversation;
import com.raven.common.model.MsgContent;
import com.raven.common.model.UserConversation;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.UidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.Map.Entry;

import static com.raven.common.utils.Constants.*;

@Slf4j
public class ConverManager {

    private RedisTemplate redisTemplate;

    public ConverManager(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private ConverManager() {
    }

    public String newSingleConverId(String fromUid, String toUid) {
        // fromUid 和 toUid总会比较大小，所以创建出来的conversation id总唯一，无论是两方谁创建。
        String converId = UidUtil.uuid24By2Factor(fromUid, toUid);
        Set<String> uidList = new HashSet<>();
        uidList.add(fromUid);
        uidList.add(toUid);
        Conversation conversation = Conversation.builder()
                .id(converId)
                .type(ConverType.SINGLE.getNumber())
                .timestamp(System.currentTimeMillis())
                .uidList(new ArrayList<>(uidList))
                .build();
        //setIfAbsent 保证并发情况
        redisTemplate.opsForValue().setIfAbsent(converId, JsonHelper.toJsonString(conversation));
        return converId;
    }

    public String newGroupConverId(String converId, List<String> members) {
        Conversation conversation = Conversation.builder()
                .id(converId)
                .type(ConverType.GROUP.getNumber())
                .timestamp(System.currentTimeMillis())
                .build();
        boolean result = redisTemplate.opsForValue()
                .setIfAbsent(converId, JsonHelper.toJsonString(conversation));
        if (result) {
            for (String member : members) {
                redisTemplate.boundSetOps(PREFIX_GROUP_MEMBER + converId).add(member);
            }
        }
        return converId;
    }

    public void addMemberConverList(String groupId, List<String> members) {
        String converId = UidUtil.uuid24ByFactor(groupId);
        for (String member : members) {
            redisTemplate.boundSetOps(PREFIX_GROUP_MEMBER + groupId).add(member);
            redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + member)
                    .put(converId, Long.MIN_VALUE);
        }
    }

    public void removeMemberConverList(String groupId, List<String> members) {
        String converId = UidUtil.uuid24ByFactor(groupId);
        for (String member : members) {
            redisTemplate.boundSetOps(PREFIX_GROUP_MEMBER + groupId).remove(member);
            redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + member).delete(converId);
        }
    }

    public void dismissGroup(String groupId) {
        String converId = UidUtil.uuid24ByFactor(groupId);
        Set<String> uids = redisTemplate
                .boundSetOps(PREFIX_GROUP_MEMBER + groupId).members();
        for (String uid : uids) {
            redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + uid).delete(converId);
        }
        redisTemplate.delete(PREFIX_GROUP_MEMBER + groupId);
        redisTemplate.delete(converId);
    }

    public boolean isSingleConverIdValid(String converId) {
        Conversation conversation = getConversation(converId);
        return conversation != null && conversation.getType() == ConverType.SINGLE.getNumber();
    }

    public List<MsgContent> getHistoryMsg(String converId, Long beginId) {
        List<MsgContent> msgContents = new ArrayList<>();
        Set<String> messages = redisTemplate.opsForZSet()
                .rangeByScore(PREFIX_MESSAGE_ID + converId, beginId + 1,
                        Long.MAX_VALUE, 0, 100);
        for (String message : messages) {
            MsgContent msgContent = JsonHelper.readValue(message, MsgContent.class);
            msgContents.add(msgContent);
        }
        return msgContents;
    }

    public long getHistoryUnReadCount(String converId, Long beginId) {
        Long unReadCount = redisTemplate.boundZSetOps(PREFIX_MESSAGE_ID + converId)
                .count(beginId + 1, Long.MAX_VALUE);
        return unReadCount;
    }

    public Conversation getConversation(String converId) {
        Object ob = redisTemplate.opsForValue().get(converId);
        if (null == ob) {
            return null;
        }
        Conversation conversation = JsonHelper.readValue(ob.toString(), Conversation.class);
        if (conversation.getType() == ConverType.GROUP.getNumber()) {
            Set<String> uids = redisTemplate
                    .boundSetOps(PREFIX_GROUP_MEMBER + conversation.getId()).members();
            conversation.setUidList(Lists.newArrayList(uids));
        }
        return conversation;
    }

    public UserConversation getConverListInfo(String uid, String converId) {
        Conversation conversation = getConversation(converId);
        if (null != conversation) {
            UserConversation converListInfo = UserConversation.builder()
                    .id(conversation.getId()).groupId(conversation.getId())
                    .uidList(conversation.getUidList())
                    .timestamp(conversation.getTimestamp())
                    .type(conversation.getType()).build();
            Long readMsgId = getUserReadMessageId(uid, converId);
            if (null != readMsgId) {
                converListInfo.setReadMsgId(readMsgId);
            }
            Set<String> strs = redisTemplate
                    .boundZSetOps(PREFIX_MESSAGE_ID + conversation.getId())
                    .range(-1, -1);
            if (strs.size() >= 1) {
                MsgContent msgContent = JsonHelper
                        .readValue(strs.iterator().next().toString(), MsgContent.class);
                converListInfo.setLastContent(msgContent);
            }
            return converListInfo;
        }
        return null;
    }

    public List<UserConversation> getConverListByUid(String uid) {
        List<UserConversation> list = new ArrayList<>();
        Map<String, Long> converList = redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + uid)
                .entries();
        for (Entry<String, Long> entry : converList.entrySet()) {
            Conversation conversation = getConversation(entry.getKey());
            if (null != conversation) {
                UserConversation converListInfo = UserConversation.builder()
                        .id(conversation.getId()).groupId(conversation.getId())
                        .uidList(conversation.getUidList()).type(conversation.getType())
                        .timestamp(conversation.getTimestamp())
                        .readMsgId(entry.getValue()).build();
                Set<String> strs = redisTemplate
                        .boundZSetOps(PREFIX_MESSAGE_ID + conversation.getId())
                        .range(-1, -1);
                if (strs.size() >= 1) {
                    MsgContent msgContent = JsonHelper
                            .readValue(strs.iterator().next(), MsgContent.class);
                    converListInfo.setLastContent(msgContent);
                }
                list.add(converListInfo);
            }
        }
        return list;
    }

    public List<String> getUidListByConver(String converId) {
        Conversation conversation = getConversation(converId);
        return conversation.getUidList();
    }

    public List<String> getUidListByConverExcludeSender(String converId, String fromUser) {
        List<String> uids = getUidListByConver(converId);
        uids.remove(fromUser);
        return uids;
    }

    public void updateUserReadMessageId(String uid, String converId, Long msgId) {
        Object oldMsgId = redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + uid).get(converId);
        if (null != oldMsgId) {
            if ((Long) oldMsgId < msgId) {
                redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + uid).put(converId, msgId);
            }
        }
    }

    public Long getUserReadMessageId(String uid, String converId) {
        Object msgId = redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + uid).get(converId);
        if (null != msgId) {
            return (Long) msgId;
        }
        return null;
    }

    public boolean isUserCidExist(String uid, Long clientId) {
        return null != redisTemplate.boundZSetOps(Constants.PREFIX_USER_CID + uid).rank(clientId);
    }

    public void saveUserCid(String uid, Long clientId) {
        redisTemplate.boundZSetOps(Constants.PREFIX_USER_CID + uid)
                .removeRangeByScore(0, System.currentTimeMillis() - (1000 * 60 * 5));
        redisTemplate
                .boundZSetOps(Constants.PREFIX_USER_CID + uid)
                .add(clientId, System.currentTimeMillis());
    }
}
