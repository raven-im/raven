package com.raven.gateway.filter;

import static com.raven.common.utils.Constants.AUTH_APP_KEY;
import static com.raven.common.utils.Constants.AUTH_NONCE;
import static com.raven.common.utils.Constants.AUTH_SIGNATURE;
import static com.raven.common.utils.Constants.AUTH_TIMESTAMP;

import com.raven.common.param.OutAppConfigParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.common.utils.JsonHelper;
import com.raven.gateway.feign.AdminFeignClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AppKeyAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private AdminFeignClient ravenAdminClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        log.info("request path:{}", path);
        if (!"/gateway/token".equals(path)) {
            return chain.filter(exchange);
        }
        HttpHeaders headers = request.getHeaders();
        String key = headers.getFirst(AUTH_APP_KEY);
        String nonce = headers.getFirst(AUTH_NONCE);
        String timestamp = headers.getFirst(AUTH_TIMESTAMP);
        String sign = headers.getFirst(AUTH_SIGNATURE);
        ServerHttpResponse response = exchange.getResponse();
        if (!isAuthPass(key, nonce, timestamp, sign)) {
            Result result = Result.failure(ResultCode.COMMON_SIGN_ERROR);
            byte[] bits = JsonHelper.toJsonString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.writeWith(Mono.just(buffer));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
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
            OutAppConfigParam response = JsonHelper
                .readValue(JsonHelper.toJsonString(result.getData()), OutAppConfigParam.class);
            return response.getSecret();
        }
        return null;
    }
}
