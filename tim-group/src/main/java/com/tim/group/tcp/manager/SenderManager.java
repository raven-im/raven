package com.tim.group.tcp.manager;

import static com.tim.common.utils.Constants.DEFAULT_SEPARATES_SIGN;
import static com.tim.common.utils.Constants.USER_ROUTE_KEY;

import com.tim.common.loadbalance.Server;
import com.tim.common.netty.ServerChannelManager;
import com.tim.common.protos.Message.UpDownMessage;
import io.netty.channel.Channel;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

        String targetId = msg.getTargetId();
        String serverAddress = (String) redisTemplate.boundHashOps(USER_ROUTE_KEY).get(targetId);

        if (!StringUtils.isEmpty(serverAddress)) {
            String[] array = serverAddress.split(DEFAULT_SEPARATES_SIGN);
            String ip = array[0];
            int port = Integer.parseInt(array[1]);
            Server server = new Server(ip, port);

            Channel chan = channelManager.getChannelByServer(server);
            if (chan != null) {
                chan.writeAndFlush(msg);
                log.info("downstream msg {} sent.", msg.getId());
            } else {
                log.error("cannot find channel. server:{}", server);
            }
        } else {
            // TODO user not online, send PUSH.
            log.info("downstream push msg {} sent.", msg.getId());
        }
    }
}
