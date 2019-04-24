package com.raven.route.config.filter;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static com.raven.common.utils.Constants.AUTH_NONCE;
import static com.raven.common.utils.Constants.AUTH_SIGNATURE;
import static com.raven.common.utils.Constants.AUTH_TIMESTAMP;

import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.common.utils.JsonHelper;
import com.raven.route.user.bean.model.AppConfigModel;
import com.raven.route.user.mapper.AppConfigMapper;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: AuthFilter
 **/
@Slf4j
//TODO
//@WebFilter(urlPatterns = {"/user/token", "/nav"}, filterName = "AuthFilter")
public class AuthFilter implements Filter {

    @Autowired
    private AppConfigMapper mapper;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String key = request.getHeader(AUTH_APP_KEY);
        String nonce = request.getHeader(AUTH_NONCE);
        String timestamp = request.getHeader(AUTH_TIMESTAMP);
        String sign = request.getHeader(AUTH_SIGNATURE);
        if (isAuthPass(key, nonce, timestamp, sign)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            Result result = Result.failure(ResultCode.COMMON_SIGN_ERROR);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().println(JsonHelper.toJsonString(result));
        }
    }

    private boolean isAuthPass(String key, String nonce, String timestamp, String sign) {
        if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(nonce) && !StringUtils
            .isEmpty(timestamp)
            && !StringUtils.isEmpty(sign)) {
            // calculate the hash.
            log.info("calculate the hash. key {} nonce {} timestamp {} sign {}", key, nonce,
                timestamp, sign);
            AppConfigModel app = getApp(key);
            if (app != null) {
                String toSign = app.getSecret() + nonce + timestamp;
                if (sign.equalsIgnoreCase(DigestUtils.sha1DigestAsHex(toSign))) {
                    return true;
                }
            }
        }
        return false;
    }

    private AppConfigModel getApp(String uid) {
        AppConfigModel model = new AppConfigModel();
        model.setUid(uid);
        return mapper.selectOne(model);
    }
}
