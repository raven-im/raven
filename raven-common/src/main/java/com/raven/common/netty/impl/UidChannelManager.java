package com.raven.common.netty.impl;

import com.raven.common.netty.IdChannelManager;
import com.raven.common.netty.NettyAttrUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static com.raven.common.netty.NettyAttrUtil.*;
import static com.raven.common.utils.Constants.DEFAULT_SEPARATOR;

@Slf4j
public class UidChannelManager implements IdChannelManager {

    //1st key: AppKey + uid,  2nd key: deviceId
    private final Map<String, Map<String, Channel>> uidChannels = new ConcurrentHashMap<>();

    @Override
    public void addUid2Channel(String uid, Channel channel, String deviceId) {
        if (!uidChannels.containsKey(uid)) {
            Map<String, Channel> map = new ConcurrentHashMap<>();
            map.put(deviceId, channel);
            uidChannels.put(uid, map);
            return;
        }
        // close that same device id if exists.   1 device == 1 channel.
        Map<String, Channel> deviceChannelMapping = uidChannels.get(uid);
        if (deviceChannelMapping.containsKey(deviceId)) {
            log.info("device [{}] old channel kicked", deviceId);
            deviceChannelMapping.get(deviceId).close();
        }
        uidChannels.get(uid).put(deviceId, channel);
    }

    @Override
    public List<Channel> getChannelsByUid(String uid) {
        if (uidChannels.containsKey(uid)) {
            List<Channel> channels = new ArrayList<>();
            for (Entry<String, Channel> entry : uidChannels.get(uid).entrySet()) {
                channels.add(entry.getValue());
            }
            return channels;
        }
        return new ArrayList<>();
    }

    @Override
    public String getUidByChannel(Channel channel) {
        String appKey = NettyAttrUtil.getAttribute(channel, ATTR_KEY_APP_KEY);
        String userId = NettyAttrUtil.getAttribute(channel, ATTR_KEY_USER_ID);
        return appKey + DEFAULT_SEPARATOR + userId;
    }

    @Override
    public void removeChannel(Channel channel) {
        String appKey = NettyAttrUtil.getAttribute(channel, ATTR_KEY_APP_KEY);
        String userId = NettyAttrUtil.getAttribute(channel, ATTR_KEY_USER_ID);
        String deviceId = NettyAttrUtil.getAttribute(channel, ATTR_KEY_DEVICE_ID);
        String uid = appKey + DEFAULT_SEPARATOR + userId;
        if (!StringUtils.isEmpty(uid) && uidChannels.containsKey(uid)) {
            Map<String, Channel> deviceChannelMapping = uidChannels.get(uid);
            deviceChannelMapping.remove(deviceId);
            if (deviceChannelMapping.isEmpty()) {
                uidChannels.remove(uid);
            }
        }
    }
//
//    @Override
//    public List<String> getAllIds() {
//        List<String> idList = new ArrayList<>();
//        for (Entry<String, List<Channel>> entry : uidChannels.entrySet()) {
//            idList.add(entry.getKey());
//        }
//        return idList;
//    }
//
//    @Override
//    public List<Channel> getAllChannels() {
//        List<Channel> channels = new ArrayList<>();
//        for (Entry<String, List<Channel>> entry : uidChannels.entrySet()) {
//            channels.addAll(entry.getValue());
//        }
//        return channels;
//    }
}
