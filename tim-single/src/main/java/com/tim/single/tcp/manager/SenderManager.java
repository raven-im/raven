package com.tim.single.tcp.manager;

import static com.tim.common.utils.Constants.*;
import com.tim.common.loadbalance.ConsistentHashLoadBalancer;
import com.tim.common.loadbalance.LoadBalancer;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ServerChannelManager;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.channel.Channel;
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
//    private static final int THREAD_NUM = Runtime.getRuntime().availableProcessors();
    private static final LinkedBlockingQueue<UpDownMessage> sendingQ = new LinkedBlockingQueue(1024 * 128);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ServerChannelManager channelManager;

    @Autowired
    private DiscoveryClient discoveryClient;

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

    public void addMessage(UpDownMessage msg) {
        sendingQ.offer(msg);
    }

    private void msgQTask(UpDownMessage msg) {
        Server server = new Server();
        String targetId = msg.getTargetId();
        String serverAddress = (String) redisTemplate.boundHashOps(USER_ROUTE_KEY).get(targetId);

        if (!StringUtils.isEmpty(serverAddress)) {
            String[] array = serverAddress.split(DEFAULT_SEPARATES_SIGN);
            String ip = array[0];
            int port = Integer.parseInt(array[1]);
            server = new Server(ip, port);
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
                server = lb.select(servers, targetId);
            }
        }

        Channel chan = channelManager.getChannelByServer(server);
        if (chan != null) {
            chan.writeAndFlush(msg);
            log.info("downstream msg {} sent.", msg.getId());
        } else {
            log.error("cannot find channel. server:{}", server);
        }
    }
}
