package com.raven.common.loadbalance;

import java.util.List;


public interface LoadBalancer {

    Server select(List<Server> servers, String hashKey);
}
