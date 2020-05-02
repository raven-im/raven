package com.raven.client.single;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.utils.SnowFlake;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ClientTo extends ImClient {

    private static final String CLIENT_UID = "test1";

    public static void main(String[] args) throws Exception {
        String token = Utils.getToken(CLIENT_UID);
        OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
        connect(serverInfo.getIp(), serverInfo.getPort(), new ClientToHandler(CLIENT_UID, token, new SnowFlake(1, 2)));
    }
}

