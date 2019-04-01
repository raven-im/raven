package com.tim.single;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class}
)
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.tim.single.restful.*.mapper")
public class TimSingleApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TimSingleApplication.class, args);
    }

}
