package com.raven.admin.user.service;

import com.raven.common.enums.AccessServerType;
import com.raven.common.result.Result;

public interface UserService {

    Result getToken(String uid, String appKey);

    Result getAccessInfo(String token, AccessServerType type);
}
