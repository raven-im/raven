package com.raven.single.tcp.manager;

import com.raven.common.loadbalance.Server;
import com.raven.common.netty.ServerChannelManager;
import com.raven.common.protos.Message.RavenMessage;
import com.raven.common.protos.Message.RavenMessage.Type;
import com.raven.common.protos.Message.UpDownMessage;
import com.raven.storage.conver.ConverManager;
import com.raven.storage.route.RouteManager;
import io.netty.channel.Channel;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author: bbpatience
 * @date: 2019/4/9
 * @description: SenderManager
 **/
@Slf4j
@Component
public class SenderManager {

    //    private static final int THREAD_NUM = Runtime.getRuntime().availableProcessors();
    private static final LinkedBlockingQueue<UpDownMessage> sendingQ = new LinkedBlockingQueue(
        1024 * 128);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ServerChannelManager channelManager;

    @Autowired
    private ConverManager converManager;

    @Autowired
    private RouteManager routeManager;

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
        List<String> uidList = converManager.getUidListByConverExcludeSender(msg.getConverId(),
            msg.getFromUid());
        for (String uid : uidList) {
            UpDownMessage downMessage = msg.toBuilder().setTargetUid(uid).build();
            Server server = routeManager.getServerByUid(uid);
            if (null != server) {
                Channel chan = channelManager.getChannelByServer(server);
                if (chan != null) {
                    RavenMessage ravenMessage = RavenMessage.newBuilder().setType(Type.UpDownMessage)
                        .setUpDownMessage(downMessage).build();
                    chan.writeAndFlush(ravenMessage);
                    log.info("send down msg {}", downMessage);
                } else {
                    log.error("cannot find channel. server:{}", server);
                }
            } else {
                converManager.incrUserConverUnCount(uid, msg.getConverId(), 1);
            }
        }

    }
}
