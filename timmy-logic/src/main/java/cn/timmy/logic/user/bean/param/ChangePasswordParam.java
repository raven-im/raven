package cn.timmy.logic.user.bean.param;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/26
 */
public class ChangePasswordParam {

    private String oldPassword;

    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
