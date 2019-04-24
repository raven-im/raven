package com.raven.route.user.service;

import com.raven.common.result.Result;

public interface UserService {

    Result getToken(String uid, String appKey);

    Result getAccessInfo(String appKey, String token);
}
