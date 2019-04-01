package com.tim.route.config.filter;

import static com.tim.common.utils.Constants.AUTH_APP_KEY;
import static com.tim.common.utils.Constants.AUTH_NONCE;
import static com.tim.common.utils.Constants.AUTH_SIGNATURE;
import static com.tim.common.utils.Constants.AUTH_TIMESTAMP;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.common.utils.GsonHelper;
import com.tim.route.user.bean.model.AppConfigModel;
import com.tim.route.user.mapper.AppConfigMapper;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: AuthFilter
 **/
@Slf4j
@WebFilter(urlPatterns = {"/user/token", "/nav"}, filterName = "AuthFilter")
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
            response.setContentType(APPLICATION_JSON_VALUE);
            OutputStream out = response.getOutputStream();
            Result result = Result.failure(ResultCode.COMMON_SIGN_ERROR);
            out.write(GsonHelper.getGson().toJson(result).getBytes());
            out.flush();
        }
    }

    private boolean isAuthPass(String key, String nonce, String timestamp, String sign) {
        if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(nonce) && !StringUtils.isEmpty(timestamp)
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
