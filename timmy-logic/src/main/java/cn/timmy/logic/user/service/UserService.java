package cn.timmy.logic.user.service;

import cn.timmy.logic.common.Result;
import cn.timmy.logic.user.bean.model.UserModel;
import cn.timmy.logic.user.bean.param.ChangePasswordParam;
import cn.timmy.logic.user.bean.param.LoginInputParam;
import cn.timmy.logic.user.bean.param.RegisterParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {

    Result register(RegisterParam param);

    Result login(HttpServletRequest request, HttpServletResponse response, LoginInputParam param);

    UserModel getUserByUsername(String username);

    UserModel getUserByUid(String uid);

    Result logout(HttpServletRequest request, HttpServletResponse response);

    Result changePassword(ChangePasswordParam param);

    Result getUserInfo(String uid);
}
