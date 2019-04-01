package com.tim.server.server;

import com.tim.server.handler.HeartBeatHandler;
import com.tim.server.handler.LoginAuthHandler;
import com.tim.server.handler.PrivateMessageHandler;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Author zxx Description 消息服务 Date Created on 2018/5/25
 */
@Component
@Slf4j
public class SingleTcpMessageServer extends BaseTcpMessageServer {

    @Autowired
    private PrivateMessageHandler privateMessageHandler;

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    @Autowired
    private LoginAuthHandler loginAuthHandler;

    @Value("${node.data-center-id}")
    private int dataCenterId;

    @Value("${node.machine-id}")
    private int machineId;

    @PostConstruct
    public void startServer() {
        addHandler("HeartBeatHandler", heartBeatHandler);
        addHandler("MessageServerHandler", loginAuthHandler);
        addHandler("privateMessageHandler", privateMessageHandler);

        super.startServer(dataCenterId, machineId);
    }

}
