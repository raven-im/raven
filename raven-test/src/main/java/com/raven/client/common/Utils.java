package com.raven.client.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.raven.client.group.bean.GroupOutParam;
import com.raven.client.group.bean.GroupReqParam;
import com.raven.common.param.OutGatewaySiteInfoParam;
import com.raven.common.protos.Message;
import com.raven.common.utils.JsonHelper;
import com.raven.common.utils.SnowFlake;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.raven.common.utils.Constants.*;

@Slf4j
public class Utils {

    public static final long CLIENT_HEART_BEAT = 15 * 1000L;
    public static final HttpClient httpClient;
    public static SnowFlake snowFlake = new SnowFlake(1, 2);

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
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8010/group/create");
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.addHeader(AUTH_APP_KEY, "u43tOdeHSx8r0XfJRuRDgo");
        httpPost.addHeader(AUTH_NONCE, "abc");
        httpPost.addHeader(AUTH_TIMESTAMP, "1565936243000");
        httpPost.addHeader(AUTH_SIGNATURE, "8b9e9323ccca5b5e78003699b0df5392404e8442");
        try {
            StringEntity stringEntity = new StringEntity(JsonHelper.toJsonString(param), "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            httpPost.setEntity(stringEntity);
            String responseBody = httpClient.execute(httpPost, responseHandler);
            JsonNode node = JsonHelper.mapper.readTree(responseBody);
            JsonNode nodeGroup = JsonHelper.mapper.readTree(node.get("data").toString());
            return new GroupOutParam(nodeGroup.get("groupId").asText());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static String getToken(String uid, String device) {
        HttpGet httpGet = new HttpGet("http://127.0.0.1:8010/access/token?uid=" + uid + "&deviceId=" + device);
        addAuthHeader(httpGet);
        String token = null;
        try {
            String responseBody = httpClient.execute(httpGet, responseHandler);
            JsonNode node = JsonHelper.mapper.readTree(responseBody);
            JsonNode nodeGroup = JsonHelper.mapper.readTree(node.get("data").toString());
            token = nodeGroup.get("token").asText();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("user[{}], token[{}]", uid, token);
        return token;
    }

    public static OutGatewaySiteInfoParam getGatewaySite(String token) {
        HttpGet httpGet = new HttpGet("http://localhost:8010/access/socket");
        addAuthHeader(httpGet);
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
        log.info("access server[{}]", outParam);
        return outParam;
    }

    private static void addAuthHeader(HttpGet httpGet) {
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader(AUTH_APP_KEY, "u43tOdeHSx8r0XfJRuRDgo");
        httpGet.addHeader(AUTH_NONCE, "abc");
        long timestamp = System.currentTimeMillis();
        httpGet.addHeader(AUTH_TIMESTAMP, String.valueOf(timestamp));
        String toSign = "aX7-E5ZyTGEkvTWQgJpMog" + "abc" + timestamp;
        String sign = DigestUtils.sha1Hex(toSign);
        httpGet.addHeader(AUTH_SIGNATURE, sign);
    }

    public static void sendLogin(ChannelHandlerContext ctx, String token) {
        Message.Login login = Message.Login.newBuilder()
                .setId(snowFlake.nextId())
                .setToken(token)
                .build();
        Message.RavenMessage ravenMessage = Message.RavenMessage.newBuilder()
                .setType(Message.RavenMessage.Type.Login)
                .setLogin(login)
                .build();
        ctx.writeAndFlush(ravenMessage);
    }

    public static void sendHeartBeat(ChannelHandlerContext ctx) {
        Message.HeartBeat heartBeat = Message.HeartBeat.newBuilder()
                .setId(snowFlake.nextId())
                .setHeartBeatType(Message.HeartBeatType.PING)
                .build();
        Message.RavenMessage ravenMessage = Message.RavenMessage.newBuilder()
                .setType(Message.RavenMessage.Type.HeartBeat)
                .setHeartBeat(heartBeat)
                .build();
        ctx.writeAndFlush(ravenMessage);
    }

    public static void rspHeartBeat(ChannelHandlerContext ctx, Message.HeartBeat heartBeat) {
//        log.info("receive heartbeat PONG {}", heartBeat.getId());
        if (heartBeat.getHeartBeatType() == Message.HeartBeatType.PONG) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sendHeartBeat(ctx);
                }
            }, CLIENT_HEART_BEAT);
        }
    }
}
