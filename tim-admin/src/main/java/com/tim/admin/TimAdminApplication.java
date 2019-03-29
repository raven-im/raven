package com.tim.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.tim.admin.mapper")
public class TimAdminApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TimAdminApplication.class, args);
    }

}
