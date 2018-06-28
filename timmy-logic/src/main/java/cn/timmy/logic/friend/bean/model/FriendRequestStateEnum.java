package cn.timmy.logic.friend.bean.model;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/28
 */
public enum FriendRequestStateEnum {

    WAITING(0),

    DELETE(1);

    private int state;

    FriendRequestStateEnum(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
