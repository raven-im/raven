package com.tim.route.config.interceptor;

import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.common.utils.GsonHelper;
import com.tim.route.admin.bean.model.AppConfigModel;
import com.tim.route.admin.service.AppConfigService;
import com.tim.route.config.annotation.NeedAuthenticated;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import static com.tim.common.utils.Constants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author: bbpatience
 * @date: 2019/3/26
 * @description: AdminInterceptor
 **/

@Component
@Slf4j
public class AdminInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private AppConfigService service;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod)handler;
            NeedAuthenticated na =  hm.getMethodAnnotation(NeedAuthenticated.class);
            if (na != null && na.needAuthenticate()) {
                String key = request.getHeader(AUTH_APP_KEY);
                String nonce = request.getHeader(AUTH_NONCE);
                String timestamp = request.getHeader(AUTH_TIMESTAMP);
                String sign = request.getHeader(AUTH_SIGNITURE);

                if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(nonce) && !StringUtils.isEmpty(timestamp)
                    && !StringUtils.isEmpty(sign)) {
                    // calculate the hash.
                    log.info("calculate the hash. key {} nonce {} timestamp {} sign {}", key, nonce,
                        timestamp, sign);
                    AppConfigModel app = service.getApp(key);
                    if (app != null) {
                        String toSign = app.getSecret() + nonce + timestamp;
                        if (sign.equalsIgnoreCase(DigestUtils.sha1DigestAsHex(toSign))) {
                            return true;
                        }
                    }
                    response.setContentType(APPLICATION_JSON_VALUE);
                    OutputStream out = response.getOutputStream();
                    Result result = Result.failure(ResultCode.COMMON_SIGN_ERROR);
                    out.write(GsonHelper.getGson().toJson(result).getBytes());
                    out.flush();
                    return false;
                }
            }
        }
        return true;
    }
}
