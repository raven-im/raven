package com.tim.single.tcp;

import com.tim.common.protos.Message.UpDownMessage;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: bbpatience
 * @date: 2019/4/9
 * @description: SenderManager
 **/
@Slf4j
public class SenderManager {
    private static final int THREAD_NUM = Runtime.getRuntime().availableProcessors();
    private static final LinkedBlockingQueue<UpDownMessage> sendingQ = new LinkedBlockingQueue(1024 * 128);

    static {
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

    private static void msgQTask(UpDownMessage msg) {

    }
}
