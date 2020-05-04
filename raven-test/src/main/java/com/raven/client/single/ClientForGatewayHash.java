package com.raven.client.single;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.common.param.OutGatewaySiteInfoParam;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ClientForGatewayHash extends ImClient {

    private static final int CAPACITY = 1000;
    private static final String PREFIX = "client_";

    public static void main(String[] args) {

        List<String> uids = new ArrayList<>(CAPACITY);

        for (int i = 0; i < CAPACITY; i++) {
            uids.add(PREFIX + i);
        }
        for (String uid : uids) {
            String token = Utils.getToken(uid, uid + "_device");
            OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
            log.info("uid [{}] access server: {}:{}", uid, serverInfo.getIp(), serverInfo.getPort());
        }
    }
}

