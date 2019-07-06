package com.raven.gateway.filter;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static com.raven.common.utils.Constants.AUTH_NONCE;
import static com.raven.common.utils.Constants.AUTH_SIGNATURE;
import static com.raven.common.utils.Constants.AUTH_TIMESTAMP;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.raven.common.param.AppConfigOutParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.common.utils.JsonHelper;
import com.raven.gateway.client.RavenAdminClient;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppKeyAuthFilter extends ZuulFilter {

    @Autowired
    private RavenAdminClient ravenAdminClient;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String uri = request.getRequestURI().toString();
        log.info("request uri:{}",uri);
        return "/raven/route/user/token".equals(uri);
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();
        String key = request.getHeader(AUTH_APP_KEY);
        String nonce = request.getHeader(AUTH_NONCE);
        String timestamp = request.getHeader(AUTH_TIMESTAMP);
        String sign = request.getHeader(AUTH_SIGNATURE);
        if (!isAuthPass(key, nonce, timestamp, sign)) {
            try {
                requestContext.setSendZuulResponse(false);
                Result result = Result.failure(ResultCode.COMMON_SIGN_ERROR);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                response.getWriter().println(JsonHelper.toJsonString(result));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private boolean isAuthPass(String key, String nonce, String timestamp, String sign) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(nonce) && StringUtils
            .isNotBlank(timestamp) && StringUtils.isNotBlank(sign)) {
            log.info("calculate the hash. key {} nonce {} timestamp {} sign {}", key, nonce,
                timestamp, sign);
            String scret = getAppSecret(key);
            if (null != scret) {
                String toSign = scret + nonce + timestamp;
                if (sign.equalsIgnoreCase(DigestUtils.sha1Hex(toSign))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getAppSecret(String uid) {
        Result result = ravenAdminClient.getApp(uid);
        if (ResultCode.COMMON_SUCCESS.getCode() == result.getCode()) {
            AppConfigOutParam response = JsonHelper
                .readValue(JsonHelper.toJsonString(result.getData()), AppConfigOutParam.class);
            return response.getSecret();
        }
        return null;
    }
}
