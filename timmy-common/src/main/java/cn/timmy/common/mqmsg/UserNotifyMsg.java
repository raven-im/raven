package cn.timmy.common.mqmsg;

import cn.timmy.common.utils.GsonHelper;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/7/1
 */
public class UserNotifyMsg {

    private Integer action;

    public UserNotifyMsg(Integer action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return GsonHelper.getGson().toJson(this, getClass());
    }

    public enum UserNotifyType {

        PASSWORD_CHANGE(2);

        private int value;

        UserNotifyType(int v) {
            this.value = v;
        }

        public int getValue() {
            return this.value;
        }
    }

}
