package com.raven.client.single;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.utils.SnowFlake;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientFrom extends ImClient {

    private static final String CLIENT_UID = "test2";
    private static final String DEVICE_UID = "test2_DEVICE";

    public static void main(String[] args) throws Exception {
        String token = Utils.getToken(CLIENT_UID, DEVICE_UID);
        OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
        connect(serverInfo.getIp(), serverInfo.getPort(), new ClientFromHandler(CLIENT_UID, token));
    }
}

