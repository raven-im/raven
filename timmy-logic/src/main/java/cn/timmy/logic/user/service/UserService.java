package cn.timmy.logic.user.service;

import cn.timmy.logic.common.Result;
import cn.timmy.logic.user.bean.LoginInputParam;
import cn.timmy.logic.user.bean.RegisterParam;
import cn.timmy.logic.user.bean.UserModel;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {

    Result register(RegisterParam param);

    Result login(HttpServletRequest request, HttpServletResponse response,LoginInputParam param);

    UserModel getUserByUsername(String username);

    Result logout(HttpServletRequest request, HttpServletResponse response);
}
