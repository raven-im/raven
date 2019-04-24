package com.raven.group.restful.validator;

import com.raven.common.result.ResultCode;
import com.raven.group.restful.bean.model.GroupModel;
import com.raven.group.restful.mapper.GroupMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: GroupValidator
 **/
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
        List<GroupModel> models = groupMapper.selectByExample(example);
        return !models.isEmpty();
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.GROUP_ERROR_INVALID_GROUP_ID;
    }
}
