package com.raven.admin.group.validator;

import com.raven.admin.group.bean.model.GroupModel;
import com.raven.admin.group.mapper.GroupMapper;
import com.raven.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

@Component
public class GroupValidator implements Validator {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public boolean isValid(String key) {
        Example example = new Example(GroupModel.class);
        example.createCriteria()
            .andEqualTo("uid", key)
            .andNotEqualTo("status", 2);
        return 0 != groupMapper.selectCountByExample(example);
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.GROUP_ERROR_INVALID_GROUP_ID;
    }
}
