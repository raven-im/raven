package com.tim.route.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Author zxx Description Date Created on 2018/6/16
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //开启spring security注解
@EnableRedisHttpSession(redisNamespace = "tim:session", maxInactiveIntervalInSeconds = 24 * 60
    * 60, cleanupCron = "0/10 * * * * *")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private RedisOperationsSessionRepository sessionRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().enableSessionUrlRewriting(false).sessionCreationPolicy(
            SessionCreationPolicy.IF_REQUIRED).maximumSessions(4)
            .sessionRegistry(sessionRegistry());
        http.csrf().disable();
        http.authorizeRequests()
            .antMatchers("/user/login", "/user/register").permitAll()
            .anyRequest().authenticated()
            .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler())
            .authenticationEntryPoint(unauthorizedEntryPoint());
    }

    @Bean
    public AuthenticationProvider loginAuthenticationProvider() {
        return new LoginAuthenticationProvider();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(loginAuthenticationProvider());
    }

    // 没有权限访问相关资源自定义处理
    @Bean
    public AccessDeniedHandler accessDeniedHandler() throws Exception {
        return new UnauthorizedHandler();
    }

    /**
     * 当未经身份验证的用户试图访问受保护的资源时调用
     */
    @Bean
    public UnauthorizedEntryPoint unauthorizedEntryPoint() throws Exception {
        return new UnauthorizedEntryPoint();
    }

    @Bean
    public SpringSessionBackedSessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setForceEncoding(true);
        characterEncodingFilter.setEncoding("UTF-8");
        registrationBean.setFilter(characterEncodingFilter);
        return registrationBean;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("TIMSESSIONID");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setCookieMaxAge(24 * 60 * 60);
        return serializer;
    }
}
