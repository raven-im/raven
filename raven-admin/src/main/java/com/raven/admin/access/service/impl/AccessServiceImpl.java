package com.raven.admin.access.service.impl;

import com.raven.admin.access.service.AccessService;
import com.raven.common.enums.GatewayServerType;
import com.raven.common.exception.TokenException;
import com.raven.common.exception.TokenExceptionType;
import com.raven.common.model.Token;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.param.OutTokenInfoParam;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.raven.common.utils.Constants.DEFAULT_SEPARATOR;

@Service
@Slf4j
public class AccessServiceImpl implements AccessService {

    @Autowired
    private com.raven.common.dubbo.AccessService dubboService;

    @Override
    public Result getToken(String appKey, String uid, String deviceId) {
        try {
            String token = new Token(appKey, uid, deviceId, System.currentTimeMillis()).getToken();
            return Result.success(new OutTokenInfoParam(appKey, uid, token));
        } catch (TokenException e) {
            return Result.failure(ResultCode.APP_ERROR_TOKEN_CREATE_ERROR);
        }
    }

    @Override
    public Result getGatewaySite(String token, GatewayServerType type) {
        try {
            Token parseToken = Token.parseFromString(token);

            //rpc access server to get the access info( consistent hash using uid).
            String accessNode = dubboService.hashRouting(parseToken.getUid());
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

        } catch (TokenException e) {
            if (TokenExceptionType.TOKEN_INVALID == e.getType()) {
                return Result.failure(ResultCode.APP_ERROR_TOKEN_INVALID);
            } else if (TokenExceptionType.TOKEN_EXPIRE == e.getType()) {
                return Result.failure(ResultCode.APP_ERROR_TOKEN_EXPIRE);
            }
        }
        log.error("no match access server.");
        return Result.failure(ResultCode.COMMON_NO_GATEWAY_ERROR);
    }
}
