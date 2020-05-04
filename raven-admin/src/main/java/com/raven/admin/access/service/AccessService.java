package com.raven.admin.access.service;

import com.raven.common.enums.GatewayServerType;
import com.raven.common.result.Result;

public interface AccessService {

    Result getToken(String appKey, String uid, String deviceId);

    Result getGatewaySite(String token, GatewayServerType type);
}
