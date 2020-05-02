package com.raven.route;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableDiscoveryClient
@ImportResource({"classpath:dubbo-provider.xml"})
public class RavenRouteApplication {

    public static void main(String[] args) {
        SpringApplication.run(RavenRouteApplication.class, args);
    }

}
