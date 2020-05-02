package com.raven.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableDiscoveryClient
@ImportResource({"classpath:dubbo.xml"})
public class RavenGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(RavenGatewayApplication.class, args);
    }

}
