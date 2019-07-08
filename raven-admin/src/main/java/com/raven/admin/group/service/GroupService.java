package com.raven.admin.group.service;

import com.raven.admin.group.bean.model.GroupModel;
import com.raven.admin.group.bean.param.GroupReqParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;

public interface GroupService {

    GroupModel createGroup(GroupReqParam reqParam);

    ResultCode joinGroup(GroupReqParam reqParam);

    ResultCode quitGroup(GroupReqParam reqParam);

    ResultCode dismissGroup(GroupReqParam reqParam);

    Result groupDetail(String groupId);
}
