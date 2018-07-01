package cn.timmy.common.utils;

import com.google.protobuf.MessageLite;
import java.io.IOException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ParseMap {

    private static final Logger logger = LogManager.getLogger(ParseMap.class);

    private static HashMap<Integer, ParseMap.Parsing> parseMap = new HashMap<>();
    public static HashMap<Class<?>, Integer> msg2ptoNum = new HashMap<>();

    @FunctionalInterface
    public interface Parsing {

        MessageLite process(byte[] bytes) throws IOException;
    }

    public static void register(int ptoNum, ParseMap.Parsing parse, Class<?> cla) {
        if (parseMap.get(ptoNum) == null) {
            parseMap.put(ptoNum, parse);
        } else {
            logger.error("pto has been registered in parseMap, ptoNum: {}", ptoNum);
            return;
        }

        if (msg2ptoNum.get(cla) == null) {
            msg2ptoNum.put(cla, ptoNum);
        } else {
            logger.error("pto has been registered in msg2ptoNum, ptoNum: {}", ptoNum);
        }
    }

    public static MessageLite getMessage(int ptoNum, byte[] bytes) throws IOException {
        Parsing parser = parseMap.get(ptoNum);
        if (parser == null) {
            logger.error("UnKnown Protocol Num: {}", ptoNum);
        }
        assert parser != null;
        return parser.process(bytes);
    }

    public static Integer getPtoNum(MessageLite msg) {
        return getPtoNum(msg.getClass());
    }

    private static Integer getPtoNum(Class<?> clz) {
        return msg2ptoNum.get(clz);
    }

}
