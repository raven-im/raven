package cn.timmy.logic.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/16
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private RedisOperationsSessionRepository sessionRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().enableSessionUrlRewriting(false).sessionCreationPolicy(
            SessionCreationPolicy.IF_REQUIRED).maximumSessions(2)
            .sessionRegistry(sessionRegistry());
        http.csrf().disable();
        http.authorizeRequests()
            .antMatchers("/user/login", "/user/register").permitAll()
            .anyRequest().authenticated()
            .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler())
            .authenticationEntryPoint(unauthorizedEntryPoint());
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new LoginAuthenticationProvider();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() throws Exception {
        return new UnauthorizedHandler();
    }

    /**
     * AccessDeniedHandler仅适用于已通过身份验。未经身份验证的用户的默认行为是重定向到登录页面
     * 需要配置一个AuthenticationEntryPoint，当未经身份验证的用户试图访问受保护的资源时调用
     */
    @Bean
    public UnauthorizedEntryPoint unauthorizedEntryPoint() throws Exception {
        return new UnauthorizedEntryPoint();
    }

    @Bean
    SpringSessionBackedSessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }
}
