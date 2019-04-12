package com.tim.group;

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
public class TimGroupTest {

    private static String MESSAGE_CONTENT = "Hello world!";
    private static String GROUP_NAME = "Tim-Group";

    /**
     * Countdown latch for Async;
     */
    private CountDownLatch lock;

    private static String groupOwner;
    private static String invitee1;
    private static String invitee2;
    private static String invitee3;

    private static String conversationId;

    private MessageAck ackMsg;

    @BeforeClass
    public static void beforeAll() {
        log.info("unit test start.");
        groupOwner = UidUtil.uuid();
        invitee1 = UidUtil.uuid();
        invitee2 = UidUtil.uuid();
        invitee3 = UidUtil.uuid();
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
    public void groupMsgAckTest() throws Exception {

        Common.MessageContent content = Common.MessageContent.newBuilder()
            .setId(1)
            .setUid(groupOwner)
            .setTime(System.currentTimeMillis())
            .setType(MessageType.TEXT)
            .setContent(MESSAGE_CONTENT)
            .build();

        UpDownMessage msg = UpDownMessage.newBuilder()
            .setId(7)
            .setFromId(groupOwner)
            .setTargetId(invitee2)
            .setConversationType(ConversationType.GROUP)
            .setContent(content)
            .setDirection(Direction.SS)
            .build();

        log.info("{} send msg to {}", groupOwner, invitee2);
        Client.sendGroupMsgTest(msg, new GroupListener() {
            @Override
            public void onMessageAckReceived(MessageAck ack) {
                log.info("get msg ACK from server");
                ackMsg = ack;
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackMsg.getTargetId(), groupOwner);
        assertEquals(ackMsg.getId(), 7);
    }
}
