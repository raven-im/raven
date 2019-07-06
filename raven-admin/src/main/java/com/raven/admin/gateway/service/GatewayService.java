package com.raven.admin.gateway.service;

import com.raven.common.enums.GatewayServerType;
import com.raven.common.result.Result;

public interface GatewayService {

    Result getToken(String uid, String appKey);

    Result getGatewaySite(String token, GatewayServerType type);
}
