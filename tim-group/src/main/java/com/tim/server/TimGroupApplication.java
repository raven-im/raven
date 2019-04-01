package com.tim.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TimGroupApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TimGroupApplication.class, args);
    }

}
