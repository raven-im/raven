package com.tim.common.utils;


import com.tim.common.protos.Ack;
import com.tim.common.protos.Auth;
import com.tim.common.protos.HeartBeat;
import com.tim.common.protos.Message;
import com.tim.common.protos.Notify;

/**
 * Author zxx Description 注册消息协议号与消息转化方法和类型 Date Created on 2018/5/12
 */
public class ParseRegistryMap {

    public static void initRegistry() {
        ParseMap.register(ProtoConstants.LOGIN, Auth.Login::parseFrom, Auth.Login.class);
        ParseMap.register(ProtoConstants.RESPONSE, Auth.Response::parseFrom, Auth.Response.class);
        ParseMap.register(ProtoConstants.UPSTREAMMESSAGE, Message.UpStreamMessage::parseFrom,
            Message.UpStreamMessage.class);
        ParseMap.register(ProtoConstants.DOWNSTREAMMESSAGE, Message.DownStreamMessage::parseFrom,
            Message.DownStreamMessage.class);
        ParseMap
            .register(ProtoConstants.ACKMESSAGE, Ack.AckMessage::parseFrom, Ack.AckMessage.class);
        ParseMap.register(ProtoConstants.BEAT, HeartBeat.Beat::parseFrom, HeartBeat.Beat.class);
        ParseMap.register(ProtoConstants.NOTIFY, Notify.NotifyMessage::parseFrom,
            Notify.NotifyMessage.class);

    }
}
