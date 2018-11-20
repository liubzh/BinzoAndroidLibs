package com.binzosoft.lib.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by binzo on 2018/4/22.
 */

public class DateTimeUtil {

    public static final String HHmmss = "HH:mm:ss";
    public static final String HHmm = "HH:mm";
    public static final String yyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMdd = "yyyy-MM-dd";

    /**
     * @param timeMillis 毫秒级时间
     * @param format     格式化字符串
     * @param timeZone   是否考虑时区，以62000毫秒为例:
     *                   timeZone 为 true：08:01:02
     *                   timeZone 为 false：00:01:02
     * @return           返回格式化后的字符串：00:01:02
     */
    public static String format(long timeMillis, String format, boolean timeZone) {
        Date date = new Date(timeMillis);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        if (!timeZone) {
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return formatter.format(date);
    }

    /**
     * @param date      毫秒级时间
     * @param format    格式化字符串
     * @param timeZone  是否考虑时区，以62000毫秒为例:
     *                  timeZone 为 true：08:01:02
     *                  timeZone 为 false：00:01:02
     * @return          返回格式化后的字符串：00:01:02
     */
    public static String format(Date date, String format, boolean timeZone) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        if (!timeZone) {
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return formatter.format(date);
    }

    public static Date parseDate(String date, String format) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(date);
    }
}
