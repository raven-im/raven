package com.tim.route.user.service;

import com.tim.common.result.Result;
import com.tim.route.user.bean.model.UserModel;
import com.tim.route.user.bean.param.ChangePasswordParam;
import com.tim.route.user.bean.param.LoginInputParam;
import com.tim.route.user.bean.param.RegisterParam;
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

    Result getToken(String uid, String appKey);
}
