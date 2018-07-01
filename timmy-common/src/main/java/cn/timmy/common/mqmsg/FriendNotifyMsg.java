package cn.timmy.common.mqmsg;

import cn.timmy.common.utils.GsonHelper;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/30
 */
public class FriendNotifyMsg {

    private Integer action;

    private Integer request_id;

    public FriendNotifyMsg(Integer action, Integer request_id) {
        this.action = action;
        this.request_id = request_id;
    }

    @Override
    public String toString() {
        return GsonHelper.getGson().toJson(this, getClass());
    }

    public enum FriendNotifyType {

        REQUEST(1),
        REPLY(2),
        ACCEPT(3);

        private int value;

        FriendNotifyType(int v) {
            this.value = v;
        }

        public int getValue() {
            return this.value;
        }
    }

}
