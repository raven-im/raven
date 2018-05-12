package gate;

import protobuf.MessageProtoNum;
import protobuf.generate.cli2srv.chat.Chat;
import protobuf.generate.cli2srv.login.Auth;

/**
 * Created by Dell on 2016/2/17.
 */
public class TransferHandlerMap {

    /**
     * 注册消息转发
     */
    public static void initRegistry() {
        ClientMessage.registerTranferHandler(MessageProtoNum.CLOGIN, ClientMessage::transfer2Auth,
                Auth.CLogin.class);
        ClientMessage
                .registerTranferHandler(MessageProtoNum.CREGISTER, ClientMessage::transfer2Auth,
                        Auth.CRegister.class);
        ClientMessage
                .registerTranferHandler(MessageProtoNum.CPRIVATECHAT, ClientMessage::transfer2Logic,
                        Chat.CPrivateChat.class);
    }
}
