package com.raven.client.multi;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.common.param.OutGatewaySiteInfoParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientFromDevice2 extends ImClient {

    public static final String CLIENT_UID = "Multi-test2";
    private static final String DEVICE_UID = "Multi-test2-DEVICE2";

    public static void main(String[] args) throws Exception {
        String token = Utils.getToken(CLIENT_UID, DEVICE_UID);
        OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
        // Use ClientToHandler, only verify receive msg.
        connect(serverInfo.getIp(), serverInfo.getPort(), new ClientToHandler(CLIENT_UID, token));
    }
}

