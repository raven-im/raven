package com.raven.admin.gateway.service.impl;

import com.raven.admin.gateway.bean.Token;
import com.raven.admin.gateway.service.GatewayService;
import com.raven.common.dubbo.AccessService;
import com.raven.common.enums.GatewayServerType;
import com.raven.common.exception.TokenException;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.param.OutTokenInfoParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;
import static com.raven.common.utils.Constants.*;

@Service
@Slf4j
public class GatewayServiceImpl implements GatewayService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AccessService accessService;

    @Override
    public Result getToken(String uid, String appKey) {
        try {
            String token = new Token(uid, appKey).getToken(appKey);
            String key = appKey + DEFAULT_SEPARATOR + uid;
            stringRedisTemplate.opsForValue().set(token, key, TOKEN_CACHE_DURATION, TimeUnit.DAYS);
            return Result.success(new OutTokenInfoParam(appKey, uid, token));
        } catch (TokenException e) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_CREATE_ERROR);
        }
    }

    @Override
    public Result getGatewaySite(String token, GatewayServerType type) {
        if (!stringRedisTemplate.hasKey(token)) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_INVALID);
        }
        String tokenStr = stringRedisTemplate.opsForValue().get(token);
        String uid = tokenStr.split(DEFAULT_SEPARATOR)[1];

        //rpc access server to get the access info( consistent hash using uid).
        String accessNode = accessService.hashRouting(uid);
        String[] nodeInfo = accessNode.split(DEFAULT_SEPARATOR);
        if (!StringUtils.isEmpty(accessNode) && nodeInfo.length == 3) {
            OutGatewaySiteInfoParam siteInfo;
            if (type == GatewayServerType.WEBSOCKET) {
                siteInfo = new OutGatewaySiteInfoParam(nodeInfo[0], Integer.parseInt(nodeInfo[2]));
                return Result.success(siteInfo);
            }
            if (type == GatewayServerType.TCP) {
                siteInfo = new OutGatewaySiteInfoParam(nodeInfo[0], Integer.parseInt(nodeInfo[1]));
                return Result.success(siteInfo);
            }
        }
        log.error("no match access server.");
        return Result.failure(ResultCode.COMMON_NO_GATEWAY_ERROR);
    }
}
