package cn.timmy.logic.user.service;

import cn.timmy.logic.common.Result;
import cn.timmy.logic.user.bean.RegisterParam;

public interface UserService {

    Result register(RegisterParam param);

    Result login();
}
