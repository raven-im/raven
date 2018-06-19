package cn.timmy.logic.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/17
 */
public class LoginAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        //1、获取用户名、密码
        String username = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();
        UserLoginDetail userDtails = (UserLoginDetail) userDetailsService
            .loadUserByUsername(username);
        Boolean check = BCrypt.checkpw(password, userDtails.getPassword());
        if (!check) {
            throw new BadCredentialsException("wrong password");
        }
        return new UsernamePasswordAuthenticationToken(userDtails.getUid(), null,
            userDtails.getAuthorities());
    }

    @Override
    public boolean supports(Class authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}
