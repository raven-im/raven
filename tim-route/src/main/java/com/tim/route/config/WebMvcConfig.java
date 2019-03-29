package com.tim.route.config;

/**
 * @author: bbpatience
 * @date: 2019/3/27
 * @description: WebMvcConfig
 **/



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import com.tim.route.config.interceptor.AdminInterceptor;

@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Autowired
    private AdminInterceptor interceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}

