package com.tim.single;

import com.tim.common.protos.Common;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Common.MessageType;
import com.tim.common.protos.Message.Direction;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.common.utils.UidUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.*;
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TimSingleTest {

    private static long MESSAGE_ID = 100;

    private static String MESSAGE_CONTENT = "Hello world!";

    /**
     * Countdown latch for Async;
     */
    private CountDownLatch lock;

    private static String fromUser;
    private static String targetUser;
    private static String conversationId;
    private MessageAck ackMsg;

    @BeforeClass
    public static void beforeAll() {
        log.info("unit test start.");
        fromUser = UidUtil.uuid();
        targetUser = UidUtil.uuid();
    }

    @Before
    public void before() {
        lock = new CountDownLatch(1);
    }
    @After
    public void after() {
        ackMsg = null;
    }

    @AfterClass
    public static void afterAll() {
        log.info("unit test end.");
    }

    @Test
    public void singleMsgAckTest() throws Exception {

        Common.MessageContent content = Common.MessageContent.newBuilder()
            .setId(1)
            .setUid(fromUser)
            .setTime(System.currentTimeMillis())
            .setType(MessageType.TEXT)
            .setContent(MESSAGE_CONTENT)
            .build();

        UpDownMessage msg = UpDownMessage.newBuilder()
            .setId(MESSAGE_ID)
            .setFromId(fromUser)
            .setTargetId(targetUser)
            .setConversationType(ConversationType.SINGLE)
            .setContent(content)
            .setDirection(Direction.SS)
            .build();

        log.info("{} send msg to {}", fromUser, targetUser);
        Client.sendSingleMsgTest(msg, (ack) -> {
            log.info("get ACK from server");
            ackMsg = ack;
            lock.countDown();
        });

        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackMsg.getTargetId(), fromUser);
        assertEquals(ackMsg.getId(), MESSAGE_ID);
        conversationId = ackMsg.getConversasionId();
    }
}
