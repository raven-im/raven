package com.tim.common.loadbalance;

import com.tim.common.TimCommonTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class LoadBalanceTest extends TimCommonTest {

    /**
     * 测试节点新增删除后的变化程度
     */
    @Test
    public void testNodeAddAndRemove() {
        // 原节点
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            servers.add(new Server(getRandomIp(), 8080));
        }
        // 上线20%节点
        List<Server> serverAdd = new ArrayList<>(120);
        servers.forEach(x -> serverAdd.add(x));
        for (int i = 0; i < 20; i++) {
            serverAdd.add(new Server(getRandomIp(), 8080));
        }
        LoadBalancer chloadBalance = new ConsistentHashLoadBalancer();
        // 构造 10000 随机请求
        List<String> invocations = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            invocations.add(UUID.randomUUID().toString());
        }
        int countDel = 0;
        // 下线20%节点
        List<Server> serverDel = servers.subList(0, 80);
        for (String invocation : invocations) {
            Server origin = chloadBalance.select(servers, invocation);
            Server delete = chloadBalance.select(serverDel, invocation);
            if (origin.getUrl().equals(delete.getUrl())) {
                countDel++;
            }
        }
        int countAdd = 0;
        for (String invocation : invocations) {
            Server origin = chloadBalance.select(servers, invocation);
            Server add = chloadBalance.select(serverAdd, invocation);
            if (origin.getUrl().equals(add.getUrl())) {
                countAdd++;
            }
        }
        log.info("下线20%节点命中率:{}", countDel / 10000D);
        log.info("上线20%节点命中率:{}", countAdd / 10000D);
    }


    // ip范围
    int[][] range = {{607649792, 608174079}, // 36.56.0.0-36.63.255.255
        {1038614528, 1039007743}, // 61.232.0.0-61.237.255.255
        {1783627776, 1784676351}, // 106.80.0.0-106.95.255.255
        {2035023872, 2035154943}, // 121.76.0.0-121.77.255.255
        {2078801920, 2079064063}, // 123.232.0.0-123.235.255.255
        {-1950089216, -1948778497}, // 139.196.0.0-139.215.255.255
        {-1425539072, -1425014785}, // 171.8.0.0-171.15.255.255
        {-1236271104, -1235419137}, // 182.80.0.0-182.92.255.255
        {-770113536, -768606209}, // 210.25.0.0-210.47.255.255
        {-569376768, -564133889}, // 222.16.0.0-222.95.255.255
    };

    /*
     * 将十进制转换成IP地址
     */
    public String num2ip(int ip) {
        int[] b = new int[4];
        String x = "";
        b[0] = (int) ((ip >> 24) & 0xff);
        b[1] = (int) ((ip >> 16) & 0xff);
        b[2] = (int) ((ip >> 8) & 0xff);
        b[3] = (int) (ip & 0xff);
        x = b[0] + "." + b[1] + "." + b[2] + "." + b[3];
        return x;
    }

    public String getRandomIp() {
        Random rdint = new Random();
        int index = rdint.nextInt(10);
        String ip = num2ip(
            range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));
        return ip;

    }
}
