package com.raven.client.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.raven.client.group.bean.GroupOutParam;
import com.raven.client.group.bean.GroupReqParam;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.utils.JsonHelper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@Slf4j
public class Utils {

    public static final HttpClient httpClient;

    public static final ResponseHandler<String> responseHandler = (response) -> {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        } else {
            log.error("Unexpected response status:{}", status);
            return null;
        }
    };

    static {
        httpClient = HttpClients.createSystem();
    }

    public static GroupOutParam newGroup(List<String> members) {
        String groupName = "test-group";
        String portrait = "http://google.com/1.jpg";
        GroupReqParam param = new GroupReqParam(groupName, portrait, members);
        HttpPost httpPost = new HttpPost("http://localhost:8080/raven/admin/group/create");
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            StringEntity stringEntity = new StringEntity(JsonHelper.toJsonString(param), "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(stringEntity);
            String responseBody = httpClient.execute(httpPost, responseHandler);
            JsonNode node = JsonHelper.mapper.readTree(responseBody);
            JsonNode nodeGroup = JsonHelper.mapper.readTree(node.get("data").toString());
            return new GroupOutParam(nodeGroup.get("groupId").asText(),
                nodeGroup.get("converId").asText());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String getToken(String uid) {
        HttpGet httpGet = new HttpGet(
            "http://localhost:8080/raven/admin/gateway/token?uid=" + uid);
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader("AppKey", "u43tOdeHSx8r0XfJRuRDgo");
        httpGet.addHeader("Nonce", "abc");
        long timestamp = System.currentTimeMillis();
        httpGet.addHeader("Timestamp", String.valueOf(timestamp));
        String toSign = "aX7-E5ZyTGEkvTWQgJpMog" + "abc" + timestamp;
        String sign = DigestUtils.sha1Hex(toSign);
        httpGet.addHeader("Sign", sign);
        String token = null;
        try {
            for (Header header : httpGet.getAllHeaders()) {
                log.info(header.toString());
            }
            String responseBody = httpClient.execute(httpGet, responseHandler);
            JsonNode node = JsonHelper.mapper.readTree(responseBody);
            JsonNode nodeGroup = JsonHelper.mapper.readTree(node.get("data").toString());
            token = nodeGroup.get("token").asText();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return token;
    }

    public static OutGatewaySiteInfoParam getGatewaySite(String token) {
        HttpGet httpGet = new HttpGet("http://localhost:8080/raven/admin/gateway");
        httpGet.addHeader("Token", token);
        OutGatewaySiteInfoParam outParam = new OutGatewaySiteInfoParam();
        try {
            String responseBody = httpClient.execute(httpGet, responseHandler);
            JsonNode node = JsonHelper.mapper.readTree(responseBody);
            JsonNode nodeGroup = JsonHelper.mapper.readTree(node.get("data").toString());
            outParam.setIp(nodeGroup.get("ip").asText());
            outParam.setPort(nodeGroup.get("port").asInt());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return outParam;
    }

}
