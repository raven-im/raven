package com.tim.route.friend.service;

import com.tim.common.result.Result;
import com.tim.route.friend.bean.param.FriendRequestParam;
import com.tim.route.friend.bean.param.FriendUpdateParam;
import com.tim.route.friend.bean.param.RequestInfoParam;
import com.tim.route.friend.bean.param.RequestReplyParam;

public interface FriendService {

    Result friendRequest(FriendRequestParam param);

    Result acceptRequest(Integer requestId);

    Result replytRequest(RequestReplyParam param);

    Result getRequestsInfo(RequestInfoParam param);

    Result getAllFriends();

    Result getFriend(String uid);

    Result deleteFriend(String uid);

    Result updateFriend(FriendUpdateParam param);

    boolean isFriend(String uid);

}
