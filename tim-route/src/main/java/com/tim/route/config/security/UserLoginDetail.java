package com.tim.route.config.security;

import java.io.Serializable;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/18
 */
public class UserLoginDetail extends User implements Serializable {

    private String uid;

    public UserLoginDetail(String username, String password,
        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
