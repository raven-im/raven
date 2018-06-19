package cn.timmy.logic.security;

import cn.timmy.logic.user.bean.UserModel;
import cn.timmy.logic.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/17
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserModel user = userService.getUserByUsername(userName);
        if (user == null) {
            throw new UsernameNotFoundException("UserName " + userName + " not found");
        }
        UserLoginDetail userLoginDetail = new UserLoginDetail(user.getUsername(),
            user.getPassword(),
            AuthorityUtils.commaSeparatedStringToAuthorityList("user"));
        userLoginDetail.setUid(user.getUid());
        return userLoginDetail;
    }
}
