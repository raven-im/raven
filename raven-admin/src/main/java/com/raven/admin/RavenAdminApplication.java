package com.raven.admin;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.raven.admin.*.mapper")
@EnableCaching
@NacosPropertySource(dataId = "raven-admin.yaml")
public class RavenAdminApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RavenAdminApplication.class, args);
    }

}
