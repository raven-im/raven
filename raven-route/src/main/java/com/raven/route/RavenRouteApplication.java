package com.raven.route;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@NacosPropertySource(dataId = "raven-route.yaml")
@EnableDiscoveryClient
public class RavenRouteApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RavenRouteApplication.class, args);
    }

}
