package com.tim.single.tcp.manager;

import static com.tim.common.utils.Constants.*;
import com.tim.common.loadbalance.ConsistentHashLoadBalancer;
import com.tim.common.loadbalance.LoadBalancer;
import com.tim.common.loadbalance.Server;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.single.tcp.client.Client;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author: bbpatience
 * @date: 2019/4/9
 * @description: SenderManager
 **/
@Slf4j
@Component
public class SenderManager {
    private static final int THREAD_NUM = Runtime.getRuntime().availableProcessors();
    private static final LinkedBlockingQueue<UpDownMessage> sendingQ = new LinkedBlockingQueue(1024 * 128);

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RedisTemplate redisTemplate;

    public SenderManager() {
        //TODO   threads create according to Thread_NUM
        new Thread(() -> {
            int sleepTime = 5;
            while (true) {
                try {
                    if (!sendingQ.isEmpty()) {
                        UpDownMessage msg = sendingQ.take();
                        if (msg != null) {
                            msgQTask(msg);
                        }
                    }
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error(e.getMessage(), e);
                }
            }
        }, "message_sender").start();
    }

    public static void addMessage(UpDownMessage msg) {
        sendingQ.offer(msg);
    }

    private void msgQTask(UpDownMessage msg) {
        String targetId = msg.getTargetId();

        // check if there is already a Access server.  if yes , dispatch to that server.
        String serverAddress = (String) redisTemplate.boundHashOps(USER_ROUTE_KEY).get(targetId);

        if (!StringUtils.isEmpty(serverAddress)) {
            String[] array = serverAddress.split(DEFAULT_SEPARATES_SIGN);
            String ip = array[0];
            long port = Long.parseLong(array[1]);
            new Client(ip, (int)port).sendSingleMsg(msg);
        } else {
            List<ServiceInstance> instances = discoveryClient.getInstances("tim-access");
            if (!instances.isEmpty()) {
                List<Server> servers = instances.stream()
                    .map((x) -> {
                        if (x.getMetadata().containsKey(CONFIG_NETTY_PORT)) {
                            int nettyPort = Integer.valueOf(x.getMetadata().get(CONFIG_NETTY_PORT));
                            return new Server(x.getHost(), nettyPort);
                        } else {
                            return new Server(x.getHost(), x.getPort());
                        }
                    })
                    .collect(Collectors.toList());
                LoadBalancer lb = new ConsistentHashLoadBalancer();
                Server origin = lb.select(servers, targetId);
                new Client(origin.getIp(), origin.getPort()).sendSingleMsg(msg);
            } else {
                log.error("no tim-access instances.");
            }
        }
    }
}
