package com.tim.route.user.service;

import com.tim.common.result.Result;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {

    Result getToken(String uid, String appKey);

    Result getAccessInfo(String appKey, String token);
}
