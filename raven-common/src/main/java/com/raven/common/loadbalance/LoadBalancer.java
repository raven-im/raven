package com.raven.common.loadbalance;

import java.util.List;


public interface LoadBalancer {

    AccessServerInfo select(List<AccessServerInfo> servers, String hashKey);
}
