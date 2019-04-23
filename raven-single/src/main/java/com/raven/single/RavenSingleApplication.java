package com.raven.single;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.raven.single.restful.mapper")
public class RavenSingleApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RavenSingleApplication.class, args);
    }

}
