package com.tim.group;

import com.tim.common.protos.Common;
import com.tim.common.protos.Common.Code;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Common.MessageType;
import com.tim.common.protos.Group.GroupAck;
import com.tim.common.protos.Group.GroupCmd;
import com.tim.common.protos.Group.GroupCmdType;
import com.tim.common.protos.Message.Direction;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.common.utils.UidUtil;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.util.StringUtils;

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

    private GroupAck ackCmd;
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
        ackCmd = null;
        ackMsg = null;
    }

    @AfterClass
    public static void afterAll() {
        log.info("unit test end.");
    }

    @Test
    public void groupCreateTest() throws Exception {
        GroupCmd createCmd = GroupCmd.newBuilder()
            .setId(1)
            .setGroupName(GROUP_NAME)
            .setType(GroupCmdType.CREATE)
            .addAllUserList(Arrays.asList(groupOwner, invitee1, invitee2))
            .build();

        log.info("create a group {}", GROUP_NAME);
        Client.sendGroupCmd(createCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 1);
        assertEquals(ackCmd.getCode(), Code.SUCCESS);
        assertFalse(StringUtils.isEmpty(ackCmd.getConversationId()));
        conversationId = ackCmd.getConversationId();
    }

    @Test
    public void groupJoinTest() throws Exception {
        GroupCmd joinCmd = GroupCmd.newBuilder()
            .setId(2)
            .setType(GroupCmdType.JOIN)
            .addAllUserList(Collections.singletonList(invitee3))
            .setConversationId(conversationId)
            .build();

        log.info("{} joins group {}", invitee3, GROUP_NAME);
        Client.sendGroupCmd(joinCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 2);
        assertEquals(ackCmd.getCode(), Code.SUCCESS);
    }

    @Test
    public void groupJoinTestFail() throws Exception {
        // invitee already in group test.
        GroupCmd joinCmd = GroupCmd.newBuilder()
            .setId(3)
            .setType(GroupCmdType.JOIN)
            .addAllUserList(Collections.singletonList(invitee2))
            .setConversationId(conversationId)
            .build();

        log.info("{} joins group {}", invitee2, GROUP_NAME);
        Client.sendGroupCmd(joinCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 3);
        assertEquals(ackCmd.getCode(), Code.FAIL);
        //TODO verify specific reason of fail.
    }

    @Test
    public void groupQuitTest() throws Exception {
        GroupCmd quitCmd = GroupCmd.newBuilder()
            .setId(4)
            .setType(GroupCmdType.QUIT)
            .addAllUserList(Collections.singletonList(invitee1))
            .setConversationId(conversationId)
            .build();

        log.info("{} quits group {}", invitee1, GROUP_NAME);
        Client.sendGroupCmd(quitCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 4);
        assertEquals(ackCmd.getCode(), Code.SUCCESS);
    }

    @Test
    public void groupQuitTestFail() throws Exception {
        // quit invitee not in group.
        String uid = UidUtil.uuid();
        GroupCmd quitCmd = GroupCmd.newBuilder()
            .setId(5)
            .setType(GroupCmdType.QUIT)
            .addAllUserList(Collections.singletonList(uid))
            .setConversationId(conversationId)
            .build();

        log.info("{} quits group {}", uid, GROUP_NAME);
        Client.sendGroupCmd(quitCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 5);
        assertEquals(ackCmd.getCode(), Code.FAIL);
        //TODO verify specific reason of fail.
    }

    @Test
    public void groupQueryTest() throws Exception {
        GroupCmd quitCmd = GroupCmd.newBuilder()
            .setId(6)
            .setType(GroupCmdType.DETAILS)
            .setConversationId(conversationId)
            .build();

        log.info("{} query group {}", groupOwner, GROUP_NAME);
        Client.sendGroupCmd(quitCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 6);
        assertEquals(ackCmd.getCode(), Code.SUCCESS);
        assertEquals(ackCmd.getDetail().getGroupName(), GROUP_NAME);
        assertTrue(ackCmd.getDetail().getUserListCount() == 3);
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

    @Test
    public void groupDismissTest() throws Exception {
        GroupCmd dismissCmd = GroupCmd.newBuilder()
            .setId(8)
            .setType(GroupCmdType.DISMISS)
            .setConversationId(conversationId)
            .build();

        log.info("{} dismiss group {}", groupOwner, GROUP_NAME);
        Client.sendGroupCmd(dismissCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 8);
        assertEquals(ackCmd.getCode(), Code.SUCCESS);
        
        // query again,  group not exists.
        GroupCmd quitCmd = GroupCmd.newBuilder()
            .setId(9)
            .setType(GroupCmdType.DETAILS)
            .setConversationId(conversationId)
            .build();

        log.info("{} query group {}", groupOwner, GROUP_NAME);
        Client.sendGroupCmd(quitCmd, new GroupListener() {
            @Override
            public void onGroupAckReceived(GroupAck ack) {
                ackCmd = ack;
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(ackCmd.getId(), 9);
        assertEquals(ackCmd.getCode(), Code.FAIL);
    }
}
