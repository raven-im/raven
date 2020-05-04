package com.raven.client.single;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.common.param.OutGatewaySiteInfoParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientTo extends ImClient {

    private static final String CLIENT_UID = "test1";
    private static final String DEVICE_UID = "test1_DEVICE";

    public static void main(String[] args) throws Exception {
        String token = Utils.getToken(CLIENT_UID, DEVICE_UID);
        OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
        connect(serverInfo.getIp(), serverInfo.getPort(), new ClientToHandler(CLIENT_UID, token));
    }
}

