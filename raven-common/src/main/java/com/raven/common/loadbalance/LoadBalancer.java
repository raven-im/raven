package com.raven.common.loadbalance;

import java.util.List;


public interface LoadBalancer {

    GatewayServerInfo select(List<GatewayServerInfo> servers, String hashKey);
}
