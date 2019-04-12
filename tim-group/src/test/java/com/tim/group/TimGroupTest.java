package com.tim.group;

import com.tim.common.protos.Common;
import com.tim.common.protos.Common.ConversationType;
import com.tim.common.protos.Common.MessageType;
import com.tim.common.protos.Message.Direction;
import com.tim.common.protos.Message.MessageAck;
import com.tim.common.protos.Message.UpDownMessage;
import com.tim.common.result.Result;
import com.tim.common.result.ResultCode;
import com.tim.common.utils.UidUtil;
import com.tim.group.restful.bean.param.GroupReqParam;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.*;
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimGroupTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static String MESSAGE_CONTENT = "Hello world!";
    private static String GROUP_NAME = "Tim-Group";
    private static String DEFAULT_URL = "http://www.google.com/1.jpg";

    /**
     * Countdown latch for Async;
     */
    private CountDownLatch lock;

    private static String groupOwner;
    private static String invitee1;
    private static String invitee2;
    private static String invitee3;

    private static String groupId;

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
    public void test1GroupCreate() {
        GroupReqParam input = new GroupReqParam();
        input.setName(GROUP_NAME);
        input.setPortrait(DEFAULT_URL);
        input.setMembers(Arrays.asList(groupOwner, invitee1, invitee2));

        Result response = restTemplate.postForObject("/group/create", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.COMMON_SUCCESS.getCode());
        Map<String, String> param = (Map) response.getData();
        groupId = param.get("groupId");
    }

    @Test
    public void test2GroupJoin() {
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(groupId);
        input.setMembers(Collections.singletonList(invitee3));

        Result response = restTemplate.postForObject("/group/join", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.COMMON_SUCCESS.getCode());
    }

    @Test
    public void test3GroupJoinFail() {
        // not valid group id.
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(UidUtil.uuid());
        input.setMembers(Collections.singletonList(invitee3));

        Result response = restTemplate.postForObject("/group/join", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.GROUP_ERROR_INVALID_GROUP_ID.getCode());
    }

    @Test
    public void test4GroupJoinFail() {
        // not valid member id.
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(groupId);
        input.setMembers(Collections.singletonList(invitee2));

        Result response = restTemplate.postForObject("/group/join", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.GROUP_ERROR_MEMBER_ALREADY_IN.getCode());
    }

    @Test
    public void test5GroupQuit() {
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(groupId);
        input.setMembers(Collections.singletonList(invitee1));

        Result response = restTemplate.postForObject("/group/quit", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.COMMON_SUCCESS.getCode());
    }

    @Test
    public void test6GroupQuitFail() {
        // not valid group id.
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(UidUtil.uuid());
        input.setMembers(Collections.singletonList(invitee2));

        Result response = restTemplate.postForObject("/group/quit", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.GROUP_ERROR_INVALID_GROUP_ID.getCode());
    }

    @Test
    public void test7GroupQuitFail() {
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(groupId);
        input.setMembers(Collections.singletonList(invitee1));

        Result response = restTemplate.postForObject("/group/quit", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.GROUP_ERROR_MEMBER_NOT_IN.getCode());
    }

    @Test
    public void test8GroupDismiss() {
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(groupId);

        Result response = restTemplate.postForObject("/group/dismiss", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.COMMON_SUCCESS.getCode());
    }

    @Test
    public void test9GroupDismissFail() {
        GroupReqParam input = new GroupReqParam();
        input.setGroupId(UidUtil.uuid());

        Result response = restTemplate.postForObject("/group/dismiss", input, Result.class);
        assertEquals(response.getCode().intValue(), ResultCode.GROUP_ERROR_INVALID_GROUP_ID.getCode());
    }

    @Test
    @Ignore
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
