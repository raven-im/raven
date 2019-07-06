package com.raven.route;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.raven.route.*.mapper")
public class RavenRouteApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RavenRouteApplication.class, args);
    }

}
