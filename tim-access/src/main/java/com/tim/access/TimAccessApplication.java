package com.tim.access;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient
public class TimAccessApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TimAccessApplication.class, args);
    }

}
