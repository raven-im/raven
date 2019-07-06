package com.raven.gateway.client;

import com.raven.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("raven-admin")
public interface RavenAdminClient {

    @GetMapping("/app/{uid}")
    Result getApp(@PathVariable("uid") String uid);

}
