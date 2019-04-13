package com.tim.single;

import static junit.framework.TestCase.assertEquals;

import com.tim.common.protos.Message.Code;
import com.tim.common.protos.Message.ConverAck;
import com.tim.common.protos.Message.ConverType;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.MessageContent;
import com.tim.common.protos.Message.MessageType;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
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
    private MessageAck ackMsg;
    private ConverAck ackConv;

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

        MessageContent content = MessageContent.newBuilder()
            .setId(1)
            .setUid(fromUser)
            .setTime(System.currentTimeMillis())
            .setType(MessageType.TEXT)
            .setContent(MESSAGE_CONTENT)
            .build();

        UpDownMessage msg = UpDownMessage.newBuilder()
            .setId(MESSAGE_ID)
            .setFromUid(fromUser)
            .setTargetUid(targetUser)
            .setConverType(ConverType.SINGLE)
            .setContent(content)
            .build();
        TimMessage timMessage = TimMessage.newBuilder().setType(Type.UpDownMessage)
            .setUpDownMessage(msg).build();

        log.info("{} send msg to {}", fromUser, targetUser);
        Client.sendSingleMsgTest(timMessage, new MessageListener() {
            @Override
            public void onMessageAckReceived(MessageAck ack) {
                log.info("get msg ACK from server");
                ackMsg = ack;
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackMsg.getTargetUid(), fromUser);
        assertEquals(ackMsg.getId(), MESSAGE_ID);
    }

    @Test
    public void singleMsgInvalidConvIdTest() throws Exception {

        MessageContent content = MessageContent.newBuilder()
            .setId(2)
            .setUid(fromUser)
            .setTime(System.currentTimeMillis())
            .setType(MessageType.TEXT)
            .setContent(MESSAGE_CONTENT)
            .build();

        String conversationId = UidUtil.uuid24By2Factor(fromUser, targetUser);
        UpDownMessage msg = UpDownMessage.newBuilder()
            .setId(MESSAGE_ID)
            .setFromUid(fromUser)
            .setTargetUid(targetUser)
            .setConverId(conversationId)
            .setConverType(ConverType.SINGLE)
            .setContent(content)
            .build();

        TimMessage timMessage = TimMessage.newBuilder().setType(Type.UpDownMessage)
            .setUpDownMessage(msg).build();

        log.info("{} send msg to {}", fromUser, targetUser);
        Client.sendSingleMsgTest(timMessage, new MessageListener() {
            @Override
            public void onMessageAckReceived(MessageAck ack) {
                log.info("get msg ACK from server");
                ackMsg = ack;
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackMsg.getTargetUid(), fromUser);
        assertEquals(ackMsg.getId(), MESSAGE_ID);
    }

    @Test
    public void singleMsgValidConvIdTest() throws Exception {

        MessageContent content = MessageContent.newBuilder()
            .setId(3)
            .setUid(fromUser)
            .setTime(System.currentTimeMillis())
            .setType(MessageType.TEXT)
            .setContent(MESSAGE_CONTENT)
            .build();

        UpDownMessage msg = UpDownMessage.newBuilder()
            .setId(MESSAGE_ID)
            .setFromUid(fromUser)
            .setTargetUid(targetUser)
            .setConverId("INVALI_CONVERSATION_ID")
            .setConverType(ConverType.SINGLE)
            .setContent(content)
            .build();
        TimMessage timMessage = TimMessage.newBuilder().setType(Type.UpDownMessage)
            .setUpDownMessage(msg).build();

        log.info("{} send msg to {}", fromUser, targetUser);
        Client.sendSingleMsgTest(timMessage, new MessageListener() {
            @Override
            public void onMessageAckReceived(MessageAck ack) {
                log.info("get msg ACK from server");
                ackMsg = ack;
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackMsg.getCode(), Code.FAIL);
    }

    @Test
    @Ignore
    public void queryConversationDetailTest() throws Exception {

//        String conversationId = UidUtil.uuid24By2Factor(fromUser, targetUser);
//        ConverReq req = ConverReq.newBuilder()
//            .setId(2)
//            .setType(OperationType.DETAIL)
//            .setConversationId(conversationId)
//            .build();
//        log.info("req conversation {}", conversationId);
//        Client.queryConversationTest(req, new MessageListener() {
//            @Override
//            public void onQueryAck(ConverAck ack) {
//                log.info("get conv ACK from server");
//                ackConv = ack;
//                lock.countDown();
//            }
//        });
//        lock.await(2000, TimeUnit.MILLISECONDS);
//        assertEquals(ackConv.getId(), 2);
//        assertEquals(ackConv.getConversation().getConverId(), conversationId);
//        assertEquals(ackConv.getConversation().getLastContent().getContent(), MESSAGE_CONTENT);
//        assertEquals(ackConv.getConversation().getType(), ConverType.SINGLE);
    }

    @Test
    @Ignore
    public void queryConversationAllTest() throws Exception {
//        ConverReq req = ConverReq.newBuilder()
//            .setId(3)
//            .setType(OperationType.ALL)
//            .build();
//        log.info("req conversation all by user {}", fromUser);
//        Client.queryConversationTest(req, new MessageListener() {
//            @Override
//            public void onQueryAck(ConverAck ack) {
//                log.info("get conv ACK from server");
//                ackConv = ack;
//                lock.countDown();
//            }
//        });
//        lock.await(2000, TimeUnit.MILLISECONDS);
//        assertEquals(ackConv.getId(), 3);
//        assertEquals(ackConv.getCode(), Code.SUCCESS);
//        assertEquals(ackConv.getConversationListCount(), 1);
    }
}
