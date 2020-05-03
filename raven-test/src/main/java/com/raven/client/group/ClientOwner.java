package com.raven.client.group;

import com.raven.client.common.ImClient;
import com.raven.client.common.Utils;
import com.raven.client.group.bean.GroupOutParam;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.utils.SnowFlake;

import java.util.Arrays;
import java.util.List;

public class ClientOwner extends ImClient {

    public static final String CLIENT_UID = "owner";

    public static void main(String[] args) throws Exception {
        List<String> members = Arrays.asList(ClientOwner.CLIENT_UID, ClientInvitee1.CLIENT_UID, ClientInvitee2.CLIENT_UID, ClientInvitee3.CLIENT_UID);
        GroupOutParam groupInfo = Utils.newGroup(members);
        String token = Utils.getToken(CLIENT_UID);
        OutGatewaySiteInfoParam serverInfo = Utils.getGatewaySite(token);
        connect(serverInfo.getIp(), serverInfo.getPort(),
                new ClientOwnerHandler(CLIENT_UID, token, new SnowFlake(1, 2),
                        groupInfo.getGroupId()));
    }
}

