package com.tim.group.restful.service.impl;

import com.tim.common.result.ResultCode;
import com.tim.common.utils.DateTimeUtils;
import com.tim.common.utils.UidUtil;
import com.tim.group.restful.bean.model.GroupMemberModel;
import com.tim.group.restful.bean.model.GroupModel;
import com.tim.group.restful.bean.param.GroupReqParam;
import com.tim.group.restful.mapper.GroupMapper;
import com.tim.group.restful.mapper.GroupMemberMapper;
import com.tim.group.restful.service.GroupService;
import com.tim.group.restful.validator.GroupValidator;
import com.tim.group.restful.validator.MemberValidator;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Throwable.class)
@Slf4j
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupMemberMapper memberMapper;
    @Autowired
    private GroupValidator groupValidator;
    @Autowired
    private MemberValidator memberValidator;

    @Override
    public GroupModel createGroup(GroupReqParam reqParam) {
        String groupId = UidUtil.uuid();
        Date now = DateTimeUtils.currentUTC();
        GroupModel model = new GroupModel();
        model.setUid(groupId);
        model.setName(reqParam.getName());
        model.setPortrait(reqParam.getPortrait());
        model.setOwner(reqParam.getMembers().get(0));
        model.setCreateDate(now);
        model.setUpdateDate(now);
        groupMapper.insert(model);

        reqParam.getMembers().forEach(uid -> {
            GroupMemberModel member = new GroupMemberModel();
            member.setGroupId(groupId);
            member.setCreateDate(now);
            member.setUpdateDate(now);
            member.setMemberUid(uid);
            memberMapper.insert(member);
        });
        return model;
    }

    @Override
    public ResultCode joinGroup(GroupReqParam reqParam) {
        //params check.
        if (!groupValidator.isValid(reqParam.getGroupId())) {
            return groupValidator.errorCode();
        }
        if (!memberValidator.isValid(reqParam.getGroupId(), reqParam.getMembers())) {
            return memberValidator.errorCode();
        }
        Date now = DateTimeUtils.currentUTC();
        reqParam.getMembers().forEach(uid-> {
            GroupMemberModel member = new GroupMemberModel();
            member.setGroupId(reqParam.getGroupId());
            member.setCreateDate(now);
            member.setUpdateDate(now);
            member.setMemberUid(uid);
            memberMapper.insert(member);
        });
        return ResultCode.COMMON_SUCCESS;
    }
}
