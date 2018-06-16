package cn.timmy.logic.common;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Author zxx
 * Description 配置
 * Date Created on 2018/6/12
 */
@Configuration
public class CustomConfig {

    /**
     * 编码过滤器
     */
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
    public HttpMessageConverters customConverters() {
        Collection<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        messageConverters.add(gsonHttpMessageConverter);
        return new HttpMessageConverters(true, messageConverters);
    }

}
