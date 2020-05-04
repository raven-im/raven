package com.raven.client.group;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.common.param.OutGatewaySiteInfoParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientInvitee1 extends ImClient {

    public static final String CLIENT_UID = "invitee1";
    private static final String DEVICE_UID = "invitee1_DEVICE";

    public static void main(String[] args) throws Exception {
        String token = Utils.getToken(CLIENT_UID, DEVICE_UID);
        OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
        connect(serverInfo.getIp(), serverInfo.getPort(), new ClientInviteeHandler(CLIENT_UID, token));
    }
}

