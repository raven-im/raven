package cn.timmy.common.utils;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/12
 */
public class DateTimeUtils {

    public static Date currentUTC() {
        return new DateTime(DateTimeZone.UTC).toDate();
    }

    private DateTimeUtils() {

    }
}
