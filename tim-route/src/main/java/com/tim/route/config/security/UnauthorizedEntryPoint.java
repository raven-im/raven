package com.tim.route.config.security;

import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.common.utils.GsonHelper;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        httpServletResponse.setContentType("application/json");
        OutputStream out = httpServletResponse.getOutputStream();
        Result result = Result.failure(ResultCode.COMMON_ERROR, e.getMessage());
        out.write(GsonHelper.getGson().toJson(result).getBytes());
        out.flush();
    }
}