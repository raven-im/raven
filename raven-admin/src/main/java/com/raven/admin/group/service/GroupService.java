package com.raven.admin.group.service;

import com.raven.admin.group.bean.model.GroupModel;
import com.raven.admin.group.bean.param.GroupReqParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;

public interface GroupService {

    GroupModel createGroup(String appKey, GroupReqParam reqParam);

    ResultCode joinGroup(String appKey, GroupReqParam reqParam);

    ResultCode quitGroup(String appKey, GroupReqParam reqParam);

    ResultCode dismissGroup(String appKey, GroupReqParam reqParam);

    Result groupDetail(String appKey, String groupId);
}
