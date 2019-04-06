package com.tim.route.user.service.impl;

import com.tim.common.exception.TokenException;
import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.common.utils.DateTimeUtils;
import com.tim.common.utils.UidUtil;
import com.tim.route.config.security.SecurityUtils;
import com.tim.route.validator.AppKeyValidator;
import com.tim.route.validator.TokenValidator;
import com.tim.route.validator.UserValidator;
import com.tim.route.user.bean.model.AppConfigModel;
import com.tim.route.user.bean.model.UserModel;
import com.tim.route.user.bean.param.*;
import com.tim.route.user.mapper.AppConfigMapper;
import com.tim.route.user.mapper.UserMapper;
import com.tim.route.user.service.UserService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.tim.route.utils.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import static com.tim.common.utils.Constants.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AppConfigMapper configMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AppKeyValidator appKeyValidator;

    @Autowired
    private TokenValidator tokenValidator;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Result register(RegisterParam param) {
        Example example = new Example(UserModel.class);
        example.createCriteria().andEqualTo("username", param.getUsername());
        List<UserModel> models = userMapper.selectByExample(example);
        if (!models.isEmpty()) {
            return Result.failure(ResultCode.COMMON_ERROR, "username exits");
        }
        String salt = BCrypt.gensalt();
        String password = BCrypt.hashpw(param.getPassword(), salt);
        UserModel model = new UserModel();
        model.setUsername(param.getUsername());
        model.setPassword(password);
        model.setCreateDate(DateTimeUtils.currentUTC());
        model.setUpdateDate(DateTimeUtils.currentUTC());
        model.setName(param.getName());
        model.setUid(UidUtil.uuid());
        userMapper.insertSelective(model);
        return Result.success();
    }

    @Override
    public Result login(HttpServletRequest request, HttpServletResponse response,
        LoginInputParam param) {
        Authentication token = new UsernamePasswordAuthenticationToken(param.getUsername(),
            param.getPassword());
        token = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(token);
        return Result.success();
    }

    @Override
    public UserModel getUserByUsername(String username) {
        Example example = new Example(UserModel.class);
        example.createCriteria().andEqualTo("username", username);
        return userMapper.selectOneByExample(example);
    }

    @Override
    public UserModel getUserByUid(String uid) {
        Example example = new Example(UserModel.class);
        example.createCriteria().andEqualTo("uid", uid);
        return userMapper.selectOneByExample(example);
    }

    @Override
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    @Override
    public Result changePassword(ChangePasswordParam param) {
        UserModel user = getUserByUid(SecurityUtils.getUid());
        boolean check = BCrypt.checkpw(param.getOld_password(), user.getPassword());
        if (!check) {
            return Result.failure(ResultCode.COMMON_ERROR, "wrong old password");
        }
        if (StringUtils.isEmpty(param.getNew_password())) {
            return Result.failure(ResultCode.COMMON_ERROR, "invalid new password");
        }
        String salt = BCrypt.gensalt();
        String password = BCrypt.hashpw(param.getNew_password(), salt);
        UserModel updateUser = new UserModel();
        updateUser.setPassword(password);
        updateUser.setUid(user.getUid());
        updateUser.setUpdateDate(DateTimeUtils.currentUTC());
        userMapper.updateByPrimaryKeySelective(updateUser);
        return Result.success();
    }

    @Override
    public Result getUserInfo(String uid) {
        UserModel model = getUserByUid(uid);
        if (null == model) {
            return Result.failure(ResultCode.COMMON_ERROR, "user not found");
        }
        UserInfoOutParam param = new UserInfoOutParam();
        param.setName(model.getName());
        param.setPortrait_url(model.getPortrait_url());
        param.setUsername(model.getUsername());
        return Result.success(param);
    }

    @Override
    public Result getToken(String uid, String appKey) {
        // check app key validation.
        if (!appKeyValidator.validate(appKey)) {
            return Result.failure(appKeyValidator.errorCode());
        }
        // check uid validation.
        if (!userValidator.validate(uid)) {
            return Result.failure(userValidator.errorCode());
        }
        //  uid:timestamp:appkey => DES(appSecret) => BASE64 => token
        try {
            String token = new Token(uid, appKey).getToken(getAppSecret(appKey));

            // cache token to redis.
            String key = appKey + DEFAULT_SEPARATES_SIGN + uid;
            redisTemplate.opsForValue().set(token, key, TOKEN_CACHE_DURATION, TimeUnit.DAYS);

            return Result.success(new TokenInfoOutParam(appKey, uid, token));
        } catch (TokenException e) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_CREATE_ERROR);
        }
    }

    @Override
    public Result getAccessInfo(String key, String token) {
        // check app key validation.
        if (!appKeyValidator.validate(key)) {
            return Result.failure(appKeyValidator.errorCode());
        }
        // check token validation.
        if (!tokenValidator.validate(token)) {
            return Result.failure(tokenValidator.errorCode());
        }
        String uid = redisTemplate.opsForValue().get(token).split(DEFAULT_SEPARATES_SIGN)[1];

        List<ServiceInstance> instances = discoveryClient.getInstances("tim-access");
        if (!instances.isEmpty()) {
            List<Server> servers = instances.stream()
                .map((x) -> new Server(x.getHost(), x.getPort()))
                .collect(Collectors.toList());
            LoadBalancer lb = new ConsistentHashLoadBalancer();
            Server origin = lb.select(servers, key + DEFAULT_SEPARATES_SIGN + uid);
            return Result
                .success(new ServerInfoOutParam(key, uid, origin.getIp(), origin.getPort()));
        }
        return Result.failure(ResultCode.COMMON_NO_ACCESS_ERROR);
    }

    private String getAppSecret(String key) {
        AppConfigModel model = new AppConfigModel();
        model.setUid(key);
        AppConfigModel app = configMapper.selectOne(model);
        return app.getSecret();
    }
}
