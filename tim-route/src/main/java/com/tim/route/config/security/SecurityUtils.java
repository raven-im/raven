package com.tim.route.config.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static boolean isLogin() {
        if (null != SecurityContextHolder.getContext().getAuthentication()) {
            boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication()
                .isAuthenticated();
            boolean isAnonymous = SecurityContextHolder.getContext()
                .getAuthentication() instanceof AnonymousAuthenticationToken;
            if (!isAnonymous) {
                return isAuthenticated;
            }
        }
        return false;
    }

    public static String getUid() {
        return String.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }
}
