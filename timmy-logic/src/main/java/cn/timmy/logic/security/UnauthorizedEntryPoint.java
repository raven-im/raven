package cn.timmy.logic.security;

import cn.timmy.common.utils.GsonHelper;
import cn.timmy.logic.common.Result;
import cn.timmy.logic.common.ResultCode;
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
        Result result = Result.failure(ResultCode.ERROR, e.getMessage());
        out.write(GsonHelper.getGson().toJson(result).getBytes());
        out.flush();
    }
}
