package com.tim.group.restful.validator;

import com.tim.common.result.ResultCode;
import com.tim.group.restful.bean.model.GroupMemberModel;
import com.tim.group.restful.mapper.GroupMemberMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: UserValidator
 **/
@Component
public class MemberInValidator implements Validator {

    @Autowired
    private GroupMemberMapper memberMapper;

    @Override
    public boolean isValid(String groupId, List<String> members) {
        //members 中有一个成员在群组中，就算失败。 members需要是一个净添加列表
        Example example = new Example(GroupMemberModel.class);
        example.createCriteria()
            .andNotEqualTo("status", 2)
            .andEqualTo("groupId", groupId)
            .andIn("memberUid", members);
        List<GroupMemberModel> models = memberMapper.selectByExample(example);
        return models.size() == 0;
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.GROUP_ERROR_MEMBER_ALREADY_IN;
    }
}
