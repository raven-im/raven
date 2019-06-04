package com.raven.common.loadbalance;

import java.util.List;


public interface LoadBalancer {

    AceessServerInfo select(List<AceessServerInfo> servers, String hashKey);
}
