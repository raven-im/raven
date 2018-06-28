package cn.timmy.logic.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static boolean isLogin() {
        return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    public static String getUid() {
        return String.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }
}
