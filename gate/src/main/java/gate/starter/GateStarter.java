package gate.starter;

import gate.GateAuthConnection;
import gate.GateMessageConnection;
import gate.GateServer;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qzy on 2016/1/28.
 */

public class GateStarter {

    private static final Logger logger = LoggerFactory.getLogger(GateStarter.class);
    private static String cfg = "gate/src/main/resources/gate.properties";
    private static int gateId;

    public static void main(String[] args) throws Exception {
        configAndStart();
    }

    private static void configAndStart() throws Exception {
        Properties prop = new Properties();
        File file = new File(cfg);
        if (file.exists()) {
            prop.load(new FileInputStream(cfg));
        } else {
            ClassLoader classLoader = GateStarter.class.getClassLoader();
            // 获取到package下的文件
            prop.load(classLoader.getResourceAsStream("gate.properties"));
        }
        gateId = Integer.parseInt(prop.getProperty("gate.id"));
        logger.info("gate id " + gateId);
        int gateListenPort = Integer.parseInt(prop.getProperty("gate.server.port"));
        String authIP = prop.getProperty("auth.ip");
        int authPort = Integer.parseInt(prop.getProperty("auth.port"));
        String messageIp = prop.getProperty("message.ip");
        int messagePort = Integer.parseInt(prop.getProperty("message.port"));
        //Start Servers
        new Thread(() -> GateServer.startGateServer(gateListenPort)).start();
        new Thread(() -> GateAuthConnection.startGateAuthConnection(authIP, authPort)).start();
        new Thread(() -> GateMessageConnection.startGateMessageConnection(messageIp, messagePort))
                .start();
    }

    public static int getGateId() {
        return gateId;
    }
}
