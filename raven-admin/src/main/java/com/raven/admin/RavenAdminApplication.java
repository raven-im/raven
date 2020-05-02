package com.raven.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.raven.admin.*.mapper")
@EnableCaching
@ImportResource({"classpath:dubbo-consumer.xml"})
public class RavenAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(RavenAdminApplication.class, args);
    }

}
