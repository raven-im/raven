package cn.timmy.logic.friend.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import cn.timmy.logic.common.Result;
import cn.timmy.logic.friend.bean.param.FriendRequestParam;
import cn.timmy.logic.friend.bean.param.FriendUpdateParam;
import cn.timmy.logic.friend.bean.param.RequestInfoParam;
import cn.timmy.logic.friend.bean.param.RequestReplyParam;
import cn.timmy.logic.friend.service.FriendService;
import cn.timmy.logic.security.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author zxx
 * Description 好友相关接口
 * Date Created on 2018/6/12
 */
@RestController
@RequestMapping(value = "/friend", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class FriendController {

    private static final Logger logger = LogManager.getLogger(
        FriendController.class);

    @Autowired
    private FriendService friendService;

    /**
     * 好友申请
     */
    @PostMapping
    public Result addFriend(@RequestBody FriendRequestParam param) {
        logger.info("uid:{} friend request to uid:{}", SecurityUtils.getUid(), param.getTo_uid());
        return friendService.friendRequest(param);
    }

    /**
     * 接受好友申请
     */
    @PutMapping("/accept/{requestId}")
    public Result acceptRequest(@PathVariable("requestId") Integer requestId) {
        logger.info("uid:{} accept request id:{}", SecurityUtils.getUid(), requestId);
        return friendService.acceptRequest(requestId);
    }

    /**
     * 回复申请
     */
    @PutMapping("/reply")
    public Result replyRequest(@RequestBody RequestReplyParam param) {
        logger.info("uid:{} reply request id:{}", SecurityUtils.getUid(), param.getId());
        return friendService.replytRequest(param);
    }

    /**
     * 获取好友申请
     */
    @PostMapping("/request_list")
    public Result getRequestsInfo(@RequestBody RequestInfoParam param) {
        logger.info("uid:{} get requests", SecurityUtils.getUid());
        return friendService.getRequestsInfo(param);
    }

    /**
     * 获取所有好友
     */
    @GetMapping
    public Result getAllFriends() {
        logger.info("uid:{} get all friends", SecurityUtils.getUid());
        return friendService.getAllFriends();
    }

    /**
     * 获取好友详情
     */
    @GetMapping("/{uid}")
    public Result getFriend(@PathVariable("uid") String uid) {
        logger.info("uid:{} get friend:{}", SecurityUtils.getUid(), uid);
        return friendService.getFriend(uid);
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{uid}")
    public Result deleteFriend(@PathVariable("uid") String uid) {
        logger.info("uid:{} delete friend:{}", SecurityUtils.getUid(), uid);
        return friendService.deleteFriend(uid);
    }

    /**
     * 修改好友信息
     */
    @PutMapping
    public Result updateFriend(FriendUpdateParam param) {
        logger.info("uid:{} update friend:{}", SecurityUtils.getUid(), param.getUid());
        return friendService.updateFriend(param);
    }

}
