package com.tim.common.utils;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Auth;
import com.tim.common.protos.Conversation;
import com.tim.common.protos.History;
import com.tim.common.protos.Message;
import com.tim.common.protos.Notify;
import java.io.IOException;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息类型与解析器绑定
 */
@Slf4j
public class ParseMap {

    private static final HashMap<Integer, ParseMap.Parsing> parseMap = new HashMap<>();

    public static final HashMap<Class<?>, Integer> msg2ptoNum = new HashMap<>();

    static {
        register(MessageTypeConstants.HEARTBEAT, Message.HeartBeat::parseFrom,
            Message.HeartBeat.class);
        register(MessageTypeConstants.LOGIN, Auth.Login::parseFrom, Auth.Login.class);
        register(MessageTypeConstants.LOGINACK, Auth.LoginAck::parseFrom, Auth.LoginAck.class);
        register(MessageTypeConstants.UPDOWNMESSAGE, Message.UpDownMessage::parseFrom,
            Message.UpDownMessage.class);
        register(MessageTypeConstants.MESSAGEACK, Message.MessageAck::parseFrom,
            Message.MessageAck.class);
        register(MessageTypeConstants.CONVERSATIONREQ, Conversation.ConverReq::parseFrom,
            Conversation.ConverReq.class);
        register(MessageTypeConstants.CONVERSATIONACK, Conversation.ConverAck::parseFrom,
            Conversation.ConverAck.class);
        register(MessageTypeConstants.CONVERSATIONDETAIL,
            Conversation.ConverInfo::parseFrom,
            Conversation.ConverInfo.class);
        register(MessageTypeConstants.HISMESSAGESREQ, History.HisMessagesReq::parseFrom,
            History.HisMessagesReq.class);
        register(MessageTypeConstants.HISMESSAGESACK, History.HisMessagesAck::parseFrom,
            History.HisMessagesAck.class);
        register(MessageTypeConstants.NOTIFY, Notify.NotifyMessage::parseFrom,
            Notify.NotifyMessage.class);
        register(MessageTypeConstants.SERVERINFO, Auth.ServerInfo::parseFrom,
            Auth.ServerInfo.class);
    }

    @FunctionalInterface
    public interface Parsing {

        MessageLite process(byte[] bytes) throws IOException;
    }

    private static void register(int messageType, Parsing parse, Class<?> cla) {
        if (parseMap.get(messageType) == null) {
            parseMap.put(messageType, parse);
        } else {
            log.error("pto has been registered in parseMap, messageType: {}", messageType);
            return;
        }

        if (msg2ptoNum.get(cla) == null) {
            msg2ptoNum.put(cla, messageType);
        } else {
            log.error("pto has been registered in msg2ptoNum, messageType: {}", messageType);
        }
    }

    public static MessageLite getMessage(int messageType, byte[] bytes) throws IOException {
        Parsing parser = parseMap.get(messageType);
        if (parser == null) {
            log.error("unknown protocol num: {}", messageType);
        }
        assert parser != null;
        return parser.process(bytes);
    }

    public static Integer getMessageType(MessageLite msg) {
        return getMessageType(msg.getClass());
    }

    private static Integer getMessageType(Class<?> clz) {
        return msg2ptoNum.get(clz);
    }

}
