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
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
//        int dstOffset = cal.get(Calendar.DST_OFFSET);
//        cal.add(Calendar.MILLISECOND, -(zoneOffset));
        return cal.getTime();
    }

    private DateTimeUtils() {

    }
}
