package com.raven.client.group;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.utils.SnowFlake;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientInvitee3 extends ImClient {
    public static final String CLIENT_UID = "invitee3";

    public static void main(String[] args) throws Exception {
        String token = Utils.getToken(CLIENT_UID);
        OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
        connect(serverInfo.getIp(), serverInfo.getPort(), new ClientInviteeHandler(CLIENT_UID, token, new SnowFlake(1, 2)));
    }
}

