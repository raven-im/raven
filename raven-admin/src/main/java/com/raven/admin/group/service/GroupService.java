package com.raven.admin.group.service;

import com.raven.admin.group.bean.model.GroupModel;
import com.raven.admin.group.bean.param.GroupReqParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;

public interface GroupService {

    Result createGroup(GroupReqParam reqParam);

    Result joinGroup(GroupReqParam reqParam);

    Result quitGroup(GroupReqParam reqParam);

    Result dismissGroup(String groupId);

    Result groupDetail(String groupId);
}
