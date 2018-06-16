package cn.timmy.common.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/12
 */
public class JbcryptUtil {

    public static String createSalt() {
        return BCrypt.gensalt();
    }

    public static String hashpw(String password, String salt) {
        return BCrypt.hashpw(password, salt);
    }

    public static boolean checkpw(String password, String code) {
        return BCrypt.checkpw(password, code);
    }

}
