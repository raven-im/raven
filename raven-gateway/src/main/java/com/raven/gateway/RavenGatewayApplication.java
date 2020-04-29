package com.raven.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ImportResource({"classpath:dubbo-consumer.xml"})
public class RavenGatewayApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RavenGatewayApplication.class, args);
    }

}
