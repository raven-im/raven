package com.tim.group.restful.service;

import com.tim.common.result.ResultCode;
import com.tim.group.restful.bean.model.GroupModel;
import com.tim.group.restful.bean.param.GroupReqParam;

public interface GroupService {
    GroupModel createGroup(GroupReqParam reqParam);
    ResultCode joinGroup(GroupReqParam reqParam);
    ResultCode quitGroup(GroupReqParam reqParam);
    ResultCode dismissGroup(GroupReqParam reqParam);
}
