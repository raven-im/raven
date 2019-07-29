package com.raven.gateway;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@NacosPropertySource(dataId = "raven-gateway.yaml")
public class RavenGatewayApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RavenGatewayApplication.class, args);
    }

}
