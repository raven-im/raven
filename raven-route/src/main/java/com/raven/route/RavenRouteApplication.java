package com.raven.route;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class RavenRouteApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RavenRouteApplication.class, args);
    }

}
