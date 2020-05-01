package com.raven.admin.config.filter;

import com.raven.admin.app.bean.model.AppConfigModel;
import com.raven.admin.app.service.AppConfigService;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.LRUCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.raven.common.utils.Constants.*;

@Component
@Order(2)
@Slf4j
public class AuthFilter implements Filter {

    private LRUCache<String, String> cache = new LRUCache<>(256);

    @Autowired
    private AppConfigService service;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();
        log.debug("request path:{}", path);
        if (path.startsWith("/app")) {
            //TODO move "Create APP Api" to a separate ADMIN to  expose a specific port to support API. not integrate to just 1 API.
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        String key = request.getHeader(AUTH_APP_KEY);
        String nonce = request.getHeader(AUTH_NONCE);
        String timestamp = request.getHeader(AUTH_TIMESTAMP);
        String sign = request.getHeader(AUTH_SIGNATURE);

        if (!isAuthPass(key, nonce, timestamp, sign)) {
            Result result = Result.failure(ResultCode.COMMON_SIGN_ERROR);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(JsonHelper.toJsonString(result));
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }
        chain.doFilter(servletRequest, servletResponse);
    }


    private boolean isAuthPass(String key, String nonce, String timestamp, String sign) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(nonce) && StringUtils
                .isNotBlank(timestamp) && StringUtils.isNotBlank(sign)) {
            log.debug("calculate the hash. key {} nonce {} timestamp {} sign {}", key, nonce,
                    timestamp, sign);
            String secret = getAppSecret(key);
            if (null != secret) {
                String toSign = secret + nonce + timestamp;
                if (sign.equalsIgnoreCase(DigestUtils.sha1Hex(toSign))) {
                    return true;
                }
            }
        }
        log.info("Auth Failed");
        return false;
    }

    private String getAppSecret(String key) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        AppConfigModel model = service.getApp(key);
        if (null != model) {
            cache.put(key, model.getSecret());
            return model.getSecret();
        }
        return null;
    }
}
