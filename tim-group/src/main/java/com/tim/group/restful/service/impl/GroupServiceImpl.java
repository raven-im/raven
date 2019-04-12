package com.tim.group.restful.service.impl;

import com.tim.common.utils.DateTimeUtils;
import com.tim.common.utils.UidUtil;
import com.tim.group.restful.bean.model.GroupMemberModel;
import com.tim.group.restful.bean.model.GroupModel;
import com.tim.group.restful.bean.param.GroupReqParam;
import com.tim.group.restful.mapper.GroupMapper;
import com.tim.group.restful.mapper.GroupMemberMapper;
import com.tim.group.restful.service.GroupService;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Throwable.class)
@Slf4j
public class GroupServiceImpl implements GroupService {

    private GroupMapper groupMapper;
    private GroupMemberMapper memberMapper;

    @Autowired
    public GroupServiceImpl(GroupMapper mapper, GroupMemberMapper memberMapper) {
        this.groupMapper = mapper;
        this.memberMapper = memberMapper;
    }

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
}
