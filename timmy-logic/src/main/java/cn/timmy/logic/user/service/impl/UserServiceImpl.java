package cn.timmy.logic.user.service.impl;

import cn.timmy.logic.common.ResultCode;
import cn.timmy.logic.user.bean.UserModel;
import cn.timmy.logic.user.service.UserService;
import cn.timmy.common.utils.DateTimeUtils;
import cn.timmy.common.utils.JbcryptUtil;
import java.util.List;
import cn.timmy.logic.common.Result;
import cn.timmy.logic.user.bean.RegisterParam;
import cn.timmy.logic.user.mapper.UserMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(
        UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Result register(RegisterParam param) {
        Example example = new Example(UserModel.class);
        example.createCriteria().andEqualTo("username", param.getUsername());
        List<UserModel> models = userMapper.selectByExample(example);
        if (!models.isEmpty()) {
            return Result.failure(ResultCode.ERROR);
        }
        String salt = JbcryptUtil.createSalt();
        String password = JbcryptUtil.hashpw(param.getPassword(), salt);
        UserModel model = new UserModel();
        model.setUsername(param.getUsername());
        model.setPassword(password);
        model.setPwdsalt(salt);
        model.setCreate_dt(DateTimeUtils.currentUTC());
        model.setName(param.getName());
        userMapper.insertSelective(model);
        return Result.success();
    }

    @Override
    public Result login() {
        return null;
    }
}
