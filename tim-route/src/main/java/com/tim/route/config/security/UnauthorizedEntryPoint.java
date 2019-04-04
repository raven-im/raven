package com.tim.route.config.security;

import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.common.utils.JsonHelper;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/19
 */
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, AuthenticationException e)
        throws IOException, ServletException {
        Result result = Result.failure(ResultCode.COMMON_UNAUTHORIZED_ERROR, e.getMessage());
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        httpServletResponse.getWriter()
            .println(JsonHelper.toJsonString(result));
    }
}
