package com.tim.common.utils;

import com.google.protobuf.MessageLite;
import com.tim.common.protos.Auth;
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
        register(MessageTypeConstants.LOGIN, Auth.Login::parseFrom, Auth.Login.class);
        register(MessageTypeConstants.LOGINACK, Auth.LoginAck::parseFrom, Auth.LoginAck.class);
        register(MessageTypeConstants.UPSINGLE, Message.UpSingle::parseFrom,
            Message.UpSingle.class);
        register(MessageTypeConstants.DOWNSINGLE, Message.DownSingle::parseFrom,
            Message.DownSingle.class);
        register(MessageTypeConstants.UPGROUP, Message.UpGroup::parseFrom, Message.UpGroup.class);
        register(MessageTypeConstants.DOWNGROUP, Message.DownGroup::parseFrom,
            Message.DownGroup.class);
        register(MessageTypeConstants.MESSAGEACK, Message.MessageAck::parseFrom,
            Message.MessageAck.class);
        register(MessageTypeConstants.HEARTBEAT, Message.HeartBeat::parseFrom,
            Message.HeartBeat.class);
        register(MessageTypeConstants.NOTIFY, Notify.NotifyMessage::parseFrom,
            Notify.NotifyMessage.class);
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
            log.error("UnKnown Protocol Num: {}", messageType);
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
