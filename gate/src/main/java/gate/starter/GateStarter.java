package gate.starter;

import gate.GateAuthConnection;
import gate.GateLogicConnection;
import gate.GateServer;
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
        prop.load(new FileInputStream(cfg));
        gateId = Integer.parseInt(prop.getProperty("gate.id"));
        logger.info("gate id " + gateId);
        int gateListenPort = Integer.parseInt(prop.getProperty("gate.server.port"));
        logger.info("gateserver gateListenPort " + gateListenPort);
        String authIP = prop.getProperty("auth.ip");
        int authPort = Integer.parseInt(prop.getProperty("auth.port"));
        logger.info("GateAuthConnection auth ip: {}  auth port: {}", authIP, authPort);
        String logicIP = prop.getProperty("logic.ip");
        int logicPort = Integer.parseInt(prop.getProperty("logic.port"));
        logger.info("GateLogicConnection logic ip: {}  logic port: {}", logicIP, logicPort);
        //Start Servers
        new Thread(() -> GateServer.startGateServer(gateListenPort)).start();
        new Thread(() -> GateAuthConnection.startGateAuthConnection(authIP, authPort)).start();
        new Thread(() -> GateLogicConnection.startGateLogicConnection(logicIP, logicPort))
                .start();
    }

    public static int getGateId() {
        return gateId;
    }
}
