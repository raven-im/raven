package com.tim.single;

import com.tim.common.protos.Common;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Common.MessageType;
import com.tim.common.protos.Conversation.ConversationAck;
import com.tim.common.protos.Conversation.ConversationReq;
import com.tim.common.protos.Conversation.OperationType;
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
import org.junit.Ignore;
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
    private ConversationAck ackConv;

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
        ackConv = null;
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
        Client.sendSingleMsgTest(msg, new MessageListener() {
            @Override
            public void onMessageAckReceived(MessageAck ack) {
                log.info("get msg ACK from server");
                ackMsg = ack;
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackMsg.getTargetId(), fromUser);
        assertEquals(ackMsg.getId(), MESSAGE_ID);
        conversationId = ackMsg.getConversasionId();
    }

    @Test
    @Ignore
    public void queryConversationDetailTest() throws Exception {
        ConversationReq req = ConversationReq.newBuilder()
            .setId(2)
            .setType(OperationType.DETAIL)
            .setConversationId(conversationId)
            .build();
        log.info("req conversation {}", conversationId);
        Client.queryConversationTest(req, new MessageListener() {
            @Override
            public void onQueryAck(ConversationAck ack) {
                log.info("get conv ACK from server");
                ackConv = ack;
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackConv.getId(), 2);
        assertEquals(ackConv.getConversation().getConversationId(), conversationId);
        assertEquals(ackConv.getConversation().getLastContent().getContent(), MESSAGE_CONTENT);
        assertEquals(ackConv.getConversation().getType(), ConversationType.SINGLE);
    }

    @Test
    @Ignore
    public void queryConversationAllTest() throws Exception {
        ConversationReq req = ConversationReq.newBuilder()
            .setId(3)
            .setType(OperationType.ALL)
            .build();
        log.info("req conversation all by user {}", fromUser);
        Client.queryConversationTest(req, new MessageListener() {
            @Override
            public void onQueryAck(ConversationAck ack) {
                log.info("get conv ACK from server");
                ackConv = ack;
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackConv.getId(), 3);
        assertEquals(ackConv.getCode(), Code.SUCCESS);
        assertEquals(ackConv.getConversationListCount(), 1);
    }
}
