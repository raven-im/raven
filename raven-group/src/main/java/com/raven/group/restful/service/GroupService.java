package com.raven.group.restful.service;

import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import com.raven.group.restful.bean.model.GroupModel;
import com.raven.group.restful.bean.param.GroupReqParam;

public interface GroupService {
    GroupModel createGroup(GroupReqParam reqParam);
    ResultCode joinGroup(GroupReqParam reqParam);
    ResultCode quitGroup(GroupReqParam reqParam);
    ResultCode dismissGroup(GroupReqParam reqParam);
    Result groupDetail(String groupId);
}
