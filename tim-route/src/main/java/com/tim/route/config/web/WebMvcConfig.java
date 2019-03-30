package com.tim.route.config.web;

import com.tim.route.config.interceptor.AdminInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport implements EnvironmentAware {

    private Environment environment;

    @Autowired
    private AdminInterceptor interceptor;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


}
