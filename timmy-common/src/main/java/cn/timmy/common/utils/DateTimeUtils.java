package cn.timmy.common.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/12
 */
public class DateTimeUtils {

    public static Date currentUTC() {
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }

    private DateTimeUtils() {

    }
}
