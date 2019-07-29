package com.raven.file;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@NacosPropertySource(dataId = "raven-file.yaml")
public class RavenFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(RavenFileApplication.class, args);
    }

}
