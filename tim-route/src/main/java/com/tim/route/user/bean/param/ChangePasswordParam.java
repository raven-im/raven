package com.tim.route.user.bean.param;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/26
 */
public class ChangePasswordParam {

    private String old_password;

    private String new_password;

    public String getOld_password() {
        return old_password;
    }

    public void setOld_password(String old_password) {
        this.old_password = old_password;
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }
}
