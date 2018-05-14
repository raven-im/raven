package protobuf;

import protobuf.analysis.ParseMap;
import protobuf.generate.cli2srv.chat.Chat;
import protobuf.generate.cli2srv.login.Auth;
import protobuf.generate.internal.Internal;

/**
 * Created by Qzy on 2016/1/30.
 */
public class ParseRegistryMap {
    /**
     * 注册消息协议号与消息转化方法和类型
     */
    public static void initRegistry() {
        ParseMap.register(MessageProtoNum.GTRANSFER, Internal.GTransfer::parseFrom, Internal.GTransfer.class); //内部传输协议用
        ParseMap.register(MessageProtoNum.GREET, Internal.Greet::parseFrom, Internal.Greet.class); //内部传输协议用
        ParseMap.register(MessageProtoNum.CLOGIN, Auth.CLogin::parseFrom, Auth.CLogin.class);
        ParseMap.register(MessageProtoNum.CREGISTER, Auth.CRegister::parseFrom, Auth.CRegister.class);
        ParseMap.register(MessageProtoNum.SRESPONSE, Auth.SResponse::parseFrom, Auth.SResponse.class);
        ParseMap.register(MessageProtoNum.CPRIVATECHAT, Chat.CPrivateChat::parseFrom, Chat.CPrivateChat.class);
        ParseMap.register(MessageProtoNum.SPRIVATECHAT, Chat.SPrivateChat::parseFrom, Chat.SPrivateChat.class);
    }
}
