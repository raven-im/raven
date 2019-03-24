package com.tim.route.friend.bean.param;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/28
 */
public class FriendsOutParam {

    private List<FriendInfo> friends = new ArrayList<>();

    public static class FriendInfo {

        public String uid;

        public String alias;

        public String portrait_url;

        public int state;

        public Date create_dt;

        public Date update_dt;

    }

    public List<FriendInfo> getFriends() {
        return friends;
    }

    public void setFriends(
        List<FriendInfo> friends) {
        this.friends = friends;
    }
}
