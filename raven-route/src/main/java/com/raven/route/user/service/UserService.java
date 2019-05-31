package com.raven.route.user.service;

import com.raven.common.result.Result;
import com.raven.route.utils.ClientType;

public interface UserService {

    Result getToken(String uid, String appKey);

    Result getAccessInfo(String token, ClientType type);
}
