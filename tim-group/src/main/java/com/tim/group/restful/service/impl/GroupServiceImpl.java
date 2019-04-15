package com.tim.group.restful.service.impl;

import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.common.utils.DateTimeUtils;
import com.tim.common.utils.UidUtil;
import com.tim.group.restful.bean.model.GroupMemberModel;
import com.tim.group.restful.bean.model.GroupModel;
import com.tim.group.restful.bean.param.GroupOutParam;
import com.tim.group.restful.bean.param.GroupReqParam;
import com.tim.group.restful.mapper.GroupMapper;
import com.tim.group.restful.mapper.GroupMemberMapper;
import com.tim.group.restful.service.GroupService;
import com.tim.group.restful.validator.GroupValidator;
import com.tim.group.restful.validator.MemberInValidator;
import com.tim.group.restful.validator.MemberNotInValidator;
import com.tim.storage.conver.ConverManager;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

@Service
@Transactional(rollbackFor = Throwable.class)
@Slf4j
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupMemberMapper memberMapper;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private MemberNotInValidator memberNotValidator;

    @Autowired
    private MemberInValidator memberInValidator;

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
        model.setStatus(0); //0 for normal
        String converId = converManager.newGroupConverId(groupId, reqParam.getMembers());
        model.setConverId(converId);
        groupMapper.insert(model);

        reqParam.getMembers().forEach(uid -> {
            GroupMemberModel member = new GroupMemberModel();
            member.setGroupId(groupId);
            member.setCreateDate(now);
            member.setUpdateDate(now);
            member.setMemberUid(uid);
            member.setStatus(0);// 0 normal status;
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
        if (!memberInValidator.isValid(reqParam.getGroupId(), reqParam.getMembers())) {
            return memberInValidator.errorCode();
        }
        Date now = DateTimeUtils.currentUTC();
        reqParam.getMembers().forEach(uid-> {
            GroupMemberModel member = new GroupMemberModel();
            member.setGroupId(reqParam.getGroupId());
            member.setCreateDate(now);
            member.setUpdateDate(now);
            member.setMemberUid(uid);
            member.setStatus(0);// 0 normal status;
            memberMapper.insert(member);
        });
        //update new member conversation list.
        converManager.addMemberConverList(reqParam.getGroupId(), reqParam.getMembers());
        return ResultCode.COMMON_SUCCESS;
    }

    @Override
    public ResultCode quitGroup(GroupReqParam reqParam) {
        //params check.
        if (!groupValidator.isValid(reqParam.getGroupId())) {
            return groupValidator.errorCode();
        }
        if (!memberNotValidator.isValid(reqParam.getGroupId(), reqParam.getMembers())) {
            return memberNotValidator.errorCode();
        }

        reqParam.getMembers().forEach(uid-> {
            GroupMemberModel member = new GroupMemberModel();
            member.setUpdateDate(DateTimeUtils.currentUTC());
            member.setStatus(2);// 2 mark delete.

            Example example = new Example(GroupMemberModel.class);
            example.createCriteria()
                .andEqualTo("groupId", reqParam.getGroupId())
                .andEqualTo("memberUid", uid);
            memberMapper.updateByExampleSelective(member, example);
        });
        //update deleted member conversation list.
        converManager.removeMemberConverList(reqParam.getGroupId(), reqParam.getMembers());
        return ResultCode.COMMON_SUCCESS;
    }

    @Override
    public ResultCode dismissGroup(GroupReqParam reqParam) {
        //params check.
        if (!groupValidator.isValid(reqParam.getGroupId())) {
            return groupValidator.errorCode();
        }
        // conversation delete.
        Example example1 = new Example(GroupMemberModel.class);
        example1.createCriteria()
            .andEqualTo("groupId", reqParam.getGroupId());
        List<GroupMemberModel> members = memberMapper.selectByExample(example1);
        List<String> memberModels = members.stream()
            .map((x) -> x.getMemberUid())
            .collect(Collectors.toList());
        converManager.removeConversation(reqParam.getGroupId(), memberModels);

        // clean group info
        GroupModel model = new GroupModel();
        model.setStatus(2); //2 for mark delete
        model.setUpdateDate(DateTimeUtils.currentUTC());
        Example example = new Example(GroupModel.class);
        example.createCriteria()
            .andEqualTo("uid", reqParam.getGroupId());
        groupMapper.updateByExampleSelective(model, example);

        //clean group member info
        GroupMemberModel member = new GroupMemberModel();
        member.setUpdateDate(DateTimeUtils.currentUTC());
        member.setStatus(2);// 2 mark delete.
        memberMapper.updateByExampleSelective(member, example1);
        return ResultCode.COMMON_SUCCESS;
    }

    @Override
    public Result groupDetail(String groupId) {
        //params check.
        if (!groupValidator.isValid(groupId)) {
            return Result.failure(groupValidator.errorCode());
        }

        Example example = new Example(GroupModel.class);
        example.createCriteria()
            .andEqualTo("status", 0)
            .andEqualTo("uid", groupId);
        List<GroupModel> info = groupMapper.selectByExample(example);

        Example example1 = new Example(GroupMemberModel.class);
        example1.createCriteria()
            .andEqualTo("status", 0)
            .andEqualTo("groupId", groupId);
        List<GroupMemberModel> members = memberMapper.selectByExample(example1);

        return Result.success(new GroupOutParam(info.get(0), members));
    }
}
