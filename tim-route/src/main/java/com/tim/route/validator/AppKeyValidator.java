package com.tim.route.validator;

import com.tim.common.result.ResultCode;
import com.tim.route.user.bean.model.AppConfigModel;
import com.tim.route.user.mapper.AppConfigMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author: bbpatience
 * @date: 2019/4/2
 * @description: AppKeyValidator
 **/
@Component
public class AppKeyValidator implements Validator {

    @Autowired
    private AppConfigMapper configMapper;

    @Override
    public boolean validate(String key) {
        Example example = new Example(AppConfigModel.class);
        example.createCriteria().andEqualTo("uid", key);
        List<AppConfigModel> models = configMapper.selectByExample(example);
        return !models.isEmpty();
    }

    @Override
    public ResultCode errorCode() {
        return ResultCode.APP_ERROR_KEY_INVALID;
    }
}
