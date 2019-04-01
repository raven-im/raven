package com.tim.route;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@EnableDiscoveryClient
@ServletComponentScan
@EnableTransactionManagement
@MapperScan("com.tim.route.*.mapper")
public class TimRouteApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TimRouteApplication.class, args);
    }

}
