package cn.timmy.logic.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.servlet.http.Cookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;

public class SecurityUtils {

    public static boolean isLogin() {
        return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    public static long getUid() {
        return Long.valueOf(
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
    }

    public static String getSid(Cookie[] cookies) {
        if (null != cookies && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if ("TIMMYSSIONID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static Object fromSerializableString(String s) {
        try {
            byte[] data = Base64.decode(s.getBytes());
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toSerializableString(Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return new String(Base64.encode(baos.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
