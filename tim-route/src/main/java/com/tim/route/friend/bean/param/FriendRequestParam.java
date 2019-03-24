package com.tim.route.friend.bean.param;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/27
 */
public class FriendRequestParam {

    private String to_uid;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getTo_uid() {
        return to_uid;
    }

    public void setTo_uid(String to_uid) {
        this.to_uid = to_uid;
    }
}
