package cn.timmy.common.utils;


import cn.timmy.common.protos.Ack;
import cn.timmy.common.protos.Auth;
import cn.timmy.common.protos.HeartBeat;
import cn.timmy.common.protos.MessageProto;

/**
 * Author zxx
 * Description 注册消息协议号与消息转化方法和类型
 * Date Created on 2018/5/12
 */
public class ParseRegistryMap {

    public static void initRegistry() {
        ParseMap.register(ProtoConstants.LOGIN, Auth.Login::parseFrom, Auth.Login.class);
        ParseMap.register(ProtoConstants.RESPONSE, Auth.Response::parseFrom, Auth.Response.class);
        ParseMap.register(ProtoConstants.UPPRIVATEMESSAGE,
            MessageProto.UpStreamMessageProto::parseFrom,
            MessageProto.UpStreamMessageProto.class);
        ParseMap.register(ProtoConstants.DOWNPRIVATEMESSAGE,
            MessageProto.DownStreamMessageProto::parseFrom,
            MessageProto.DownStreamMessageProto.class);
        ParseMap.register(ProtoConstants.MESSAGERES, Ack.MessageRes::parseFrom, Ack.MessageRes.class);
        ParseMap.register(ProtoConstants.BEAT, HeartBeat.Beat::parseFrom, HeartBeat.Beat.class);

    }
}
