package com.binzosoft.lib.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by binzo on 2018/4/22.
 */

public class DateTimeUtil {

    public static final String HHmmss = "HH:mm:ss";
    public static final String HHmm = "HH:mm";
    public static final String yyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMdd = "yyyy-MM-dd";

    public static String format(long timeMillis, String format) {
        if (HHmm.equals(format)) {
            timeMillis = timeMillis % (24 * 3600 * 1000);
            int h = (int) (timeMillis / 3600000);
            int m = (int) ((timeMillis / 60000) % 60);
            StringBuffer sb = new StringBuffer();
            if (h < 10) {
                sb.append("0");
            }
            sb.append(h).append(":");
            if (m < 10) {
                sb.append("0");
            }
            sb.append(m);
            return sb.toString();
        } else if (HHmmss.equals(format)) {
            timeMillis = timeMillis % (24 * 3600 * 1000);
            int h = (int) (timeMillis / 3600000);
            int m = (int) ((timeMillis / 60000) % 60);
            int s = (int) ((timeMillis / 1000) % 60);
            StringBuffer sb = new StringBuffer();
            if (h < 10) {
                sb.append("0");
            }
            sb.append(h).append(":");
            if (m < 10) {
                sb.append("0");
            }
            sb.append(m).append(":");
            if (s < 10) {
                sb.append("0");
            }
            sb.append(s);
            return sb.toString();
        } else {
            Date date = new Date(timeMillis);
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            String result = formatter.format(date);
            return result;
        }
    }

    public static String format(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String time = formatter.format(date);
        return time;
    }

    /**
     * 相应时区
     * @return
     */
    public static long currentTimeMillis() {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }

    public static Date parseDate(String date, String format) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(date);
    }
}
