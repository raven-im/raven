package com.tim.single;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TimSingleApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TimSingleApplication.class, args);
    }

}
