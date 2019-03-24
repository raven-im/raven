package com.tim.route.friend.bean.model;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/27
 */
public enum FriendStateEnum {

    NORMAL(0),

    BLACK(1);

    private int state;

    FriendStateEnum(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
