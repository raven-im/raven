package com.raven.storage.conver;

import static com.raven.common.utils.Constants.PREFIX_CONVERSATION_LIST;
import static com.raven.common.utils.Constants.PREFIX_GROUP_MEMBER;
import static com.raven.common.utils.Constants.PREFIX_MESSAGE_ID;
import static com.raven.common.utils.Constants.DEFAULT_SEPARATES_SIGN;

import com.google.common.collect.Lists;
import com.raven.common.model.Conversation;
import com.raven.common.model.NotifyContent;
import com.raven.common.model.UserConversation;
import com.raven.common.model.MsgContent;
import com.raven.common.protos.Message.NotifyMessage;
import com.raven.common.protos.Message.ConverType;
import com.raven.common.protos.Message.MessageContent;
import com.raven.common.protos.Message.MessageType;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.common.utils.Constants;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.UidUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;

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
        Conversation conversation = new Conversation().builder().id(converId)
            .type(ConverType.SINGLE.getNumber())
            .time(System.currentTimeMillis())
            .uidList(new ArrayList<>(uidList)).build();
        boolean result = redisTemplate.opsForValue()
            .setIfAbsent(converId, JsonHelper.toJsonString(conversation));
        if (result) {
            redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + fromUid)
                .put(converId, Long.MIN_VALUE);
            redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + toUid)
                .put(converId, Long.MIN_VALUE);
        }
        return converId;
    }

    public String newGroupConverId(String groupId, List<String> members) {
        String converId = UidUtil.uuid24ByFactor(groupId);
        Conversation conversation = new Conversation().builder().id(converId)
            .type(ConverType.GROUP.getNumber())
            .time(System.currentTimeMillis())
            .groupId(groupId).build();
        boolean result = redisTemplate.opsForValue()
            .setIfAbsent(converId, JsonHelper.toJsonString(conversation));
        if (result) {
            for (String member : members) {
                redisTemplate.boundSetOps(PREFIX_GROUP_MEMBER + groupId).add(member);
                redisTemplate.boundHashOps(PREFIX_CONVERSATION_LIST + member)
                    .put(converId, Long.MIN_VALUE);
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
        return conversation == null ? false
            : conversation.getType() == ConverType.SINGLE.getNumber();
    }

    public String getGroupIdByConverId(String converId) {
        Conversation conversation = getConversation(converId);
        if (conversation == null ? false : conversation.getType() == ConverType.GROUP.getNumber()) {
            return conversation.getGroupId();
        }
        return null;
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
                .boundSetOps(PREFIX_GROUP_MEMBER + conversation.getGroupId()).members();
            conversation.setUidList(Lists.newArrayList(uids));
        }
        return conversation;
    }

    public UserConversation getConverListInfo(String uid, String converId) {
        Conversation conversation = getConversation(converId);
        if (null != conversation) {
            UserConversation converListInfo = new UserConversation().builder()
                .id(conversation.getId()).groupId(conversation.getGroupId())
                .uidList(conversation.getUidList())
                .time(conversation.getTime())
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
                UserConversation converListInfo = new UserConversation().builder()
                    .id(conversation.getId()).groupId(conversation.getGroupId())
                    .uidList(conversation.getUidList()).type(conversation.getType())
                    .time(conversation.getTime())
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

    public void saveWaitUserAckMsg(String uid, String converId, Long msgId) {
        String key = Constants.PREFIX_WAIT_USER_ACK_MID + uid;
        redisTemplate.boundZSetOps(key)
            .add(converId + DEFAULT_SEPARATES_SIGN + msgId, System.currentTimeMillis());
        log.info("save user:{} wait ack msg,converId:{},msgId:{}", uid, converId, msgId);
    }

    public void delWaitUserAckMsg(String uid, String converId, Long msgId) {
        String key = Constants.PREFIX_WAIT_USER_ACK_MID + uid;
        redisTemplate.boundZSetOps(key).remove(converId + DEFAULT_SEPARATES_SIGN + msgId);
        log.info("delete user:{} wait ack msg,converId:{},msgId:{}", uid, converId, msgId);
    }

    public List<UpDownMessage> getWaitUserAckMsg(String uid) {
        String key = Constants.PREFIX_WAIT_USER_ACK_MID + uid;
        Set<String> keyList = redisTemplate.opsForZSet()
            .rangeByScore(key, 0, System.currentTimeMillis() - 5000, 0, 3);
        List<UpDownMessage> upDownMessages = new ArrayList<>();
        for (String str : keyList) {
            log.info("not ack msg key:{}", str);
            String converId = str.split(DEFAULT_SEPARATES_SIGN)[0];
            String msgId = str.split(DEFAULT_SEPARATES_SIGN)[1];
            Set<String> messages = redisTemplate.opsForZSet()
                .rangeByScore(PREFIX_MESSAGE_ID + converId, Long.valueOf(msgId),
                    Long.valueOf(msgId), 0, 1);
            if (CollectionUtils.isNotEmpty(messages)) {
                MsgContent msgContent = JsonHelper
                    .readValue(messages.iterator().next(), MsgContent.class);
                MessageContent content = MessageContent.newBuilder()
                    .setId(msgContent.getId())
                    .setUid(msgContent.getUid())
                    .setContent(msgContent.getContent())
                    .setTime(msgContent.getTime())
                    .setType(MessageType.valueOf(msgContent.getType())).build();
                Conversation conversation = getConversation(converId);
                UpDownMessage downMessage = UpDownMessage.newBuilder()
                    .setId(msgContent.getId())
                    .setFromUid(msgContent.getUid())
                    .setTargetUid(uid)
                    .setConverType(ConverType.forNumber(conversation.getType()))
                    .setContent(content)
                    .setConverId(converId)
                    .build();
                upDownMessages.add(downMessage);
            }
        }
        return upDownMessages;
    }

}
