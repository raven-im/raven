package protobuf;

import protobuf.analysis.ParseMap;
import protobuf.protos.Auth;
import protobuf.protos.PrivateMessageProto;

/**
 * Created by Qzy on 2016/1/30.
 */
public class ParseRegistryMap {

    /**
     * 注册消息协议号与消息转化方法和类型
     */
    public static void initRegistry() {
        ParseMap.register(MessageProtoNum.LOGIN, Auth.Login::parseFrom, Auth.Login.class);
        ParseMap.register(MessageProtoNum.RESPONSE, Auth.Response::parseFrom, Auth.Response.class);
        ParseMap.register(MessageProtoNum.PRIVATEMESSAGE,
                PrivateMessageProto.UpStreamMessageProto::parseFrom,
                PrivateMessageProto.UpStreamMessageProto.class);
    }
}
