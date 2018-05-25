package message.utils;

import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import java.io.IOException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMessage {

    private static final Logger logger = LoggerFactory.getLogger(ClientMessage.class);

    private static HashMap<Integer, Transfer> tranferHandlerMap = new HashMap<>();
    private static HashMap<Class<?>, Integer> msg2ptoNum = new HashMap<>();

    @FunctionalInterface
    public interface Transfer {

        void process(MessageLite msg, Connection conn) throws IOException;
    }

    public static void registerTranferHandler(Integer ptoNum, Transfer tranfer, Class<?> cla) {
        if (tranferHandlerMap.get(ptoNum) == null) {
            tranferHandlerMap.put(ptoNum, tranfer);
        } else {
            logger.error("pto has been registered in transeerHandlerMap, ptoNum: {}", ptoNum);
            return;
        }
        if (msg2ptoNum.get(cla) == null) {
            msg2ptoNum.put(cla, ptoNum);
        } else {
            logger.error("pto has been registered in msg2ptoNum, ptoNum: {}", ptoNum);
        }
    }

    public static void processTransferHandler(Message msg, Connection conn)
            throws IOException {
        logger.info("MessageName {}", msg.getClass());
        int ptoNum = msg2ptoNum.get(msg.getClass());
        Transfer transferHandler = tranferHandlerMap.get(ptoNum);
        if (transferHandler != null) {
            transferHandler.process(msg, conn);
        }
    }


}
