package com.raven.client.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.raven.client.group.bean.GroupOutParam;
import com.raven.client.group.bean.GroupReqParam;
import com.raven.common.utils.JsonHelper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Utils {

    public static GroupOutParam newGroup() {
        String groupName = "test_group";
        String portrait = "http://google.com/1.jpg";
        List<String> members = Arrays.asList("owner", "invitee1", "invitee2");
        GroupReqParam param = new GroupReqParam(groupName, portrait, members);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8370/group/create");
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            StringEntity stringEntity = new StringEntity(JsonHelper.toJsonString(param), "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(stringEntity);
            ResponseHandler<String> responseHandler = (response) -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody = httpclient.execute(httpPost, responseHandler);
            JsonNode node = JsonHelper.getJacksonMapper().readTree(responseBody);
            JsonNode nodeGroup = JsonHelper.getJacksonMapper()
                .readTree(node.get("data").toString());
            return new GroupOutParam(nodeGroup.get("groupId").asText(),
                nodeGroup.get("converId").asText());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getToken(String uid) {
        HttpGet httpGet = new HttpGet("http://localhost:8040/user/token?uid=" + uid);
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader("AppKey", "u43tOdeHSx8r0XfJRuRDgo");
        httpGet.addHeader("Nonce", "abc");
        long timestamp = System.currentTimeMillis();
        httpGet.addHeader("Timestamp", String.valueOf(timestamp));
        String toSign = "aX7-E5ZyTGEkvTWQgJpMog" + "abc" + timestamp;
        String sign = DigestUtils.sha1Hex(toSign);
        httpGet.addHeader("Sign", sign);
        ResponseHandler<String> responseHandler = (response) -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String token = null;
        try {
            String responseBody = httpclient.execute(httpGet, responseHandler);
            JsonNode node = JsonHelper.getJacksonMapper().readTree(responseBody);
            JsonNode nodeGroup = JsonHelper.getJacksonMapper()
                .readTree(node.get("data").toString());
            token = nodeGroup.get("token").toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return token;
    }

}
