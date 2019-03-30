package com.tim.route.user.bean.param;

import lombok.Data;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/26
 */
@Data
public class ChangePasswordParam {

    private String old_password;

    private String new_password;
}
