package cn.timmy.logic.friend.service;

import cn.timmy.logic.common.Result;
import cn.timmy.logic.friend.bean.param.FriendRequestParam;
import cn.timmy.logic.friend.bean.param.FriendUpdateParam;
import cn.timmy.logic.friend.bean.param.RequestInfoParam;
import cn.timmy.logic.friend.bean.param.RequestReplyParam;

public interface FriendService {

    Result friendRequest(FriendRequestParam param);

    Result acceptRequest(int id);

    Result replytRequest(RequestReplyParam param);

    Result getRequestsInfo(RequestInfoParam param);

    Result getAllFriends();

    Result getFriend(String uid);

    Result deleteFriend(String uid);

    Result updateFriend(FriendUpdateParam param);

    boolean isFriend(String uid);

}
