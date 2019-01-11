package com.binzosoft.lib.caption;

public class TimeUtil {

    public static final String FORMAT_HH_MM_SS_MMM = "hh:mm:ss,mmm";

    /**
     * @param format 指定解析时间的格式
     * @param formattedTime 格式化之后的时间。比如：01:23:03,230
     * @return 解析出来的毫秒时间值，milliseconds
     */
    public static long valueOf(String format, String formattedTime) {
        if (FORMAT_HH_MM_SS_MMM.equals(format)) {
            int h, m, s, ms;
            h = Integer.parseInt(formattedTime.substring(0, 2));
            m = Integer.parseInt(formattedTime.substring(3, 5));
            s = Integer.parseInt(formattedTime.substring(6, 8));
            String msecond = formattedTime.substring(formattedTime.lastIndexOf(",") + 1);
            if (msecond.length() == 2) {
                msecond = msecond + "0";
            }
            ms = Integer.parseInt(msecond);
            //System.out.println(String.format("%d:%d:%d,%d", h, m, s, ms));
            return ms + s * 1000 + m * 60000 + h * 3600000;
        }
        return -1;
    }

    public static String format(String format, long mseconds) {
        //we use string builder for efficiency
        StringBuilder time = new StringBuilder();
        String aux;
        if (format.equalsIgnoreCase(FORMAT_HH_MM_SS_MMM)) {
            // this type of format:  01:02:22,501 (used in .SRT)
            int h, m, s, ms;
            h = (int) (mseconds / 3600000);
            m = (int) (mseconds / 60000 % 60);
            s = (int) (mseconds / 1000 % 60);
            ms = (int) (mseconds % 1000);
            return String.format("%02d:%02d:%02d,%03d", h, m, s, ms);
        } else if (format.equalsIgnoreCase("h:mm:ss.cs")) {
            // this type of format:  1:02:22.51 (used in .ASS/.SSA)
            int h, m, s, cs;
            h = (int) (mseconds / 3600000);
            aux = String.valueOf(h);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            m = (int) ((mseconds / 60000) % 60);
            aux = String.valueOf(m);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            s = (int) ((mseconds / 1000) % 60);
            aux = String.valueOf(s);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append('.');
            cs = (int) ((mseconds / 10) % 100);
            aux = String.valueOf(cs);
            if (aux.length() == 1) time.append('0');
            time.append(aux);

        } else if (format.startsWith("hhmmssff/")) {
            //this format is used in EBU's STL
            int h, m, s, f;
            float fps;
            String[] args = format.split("/");
            fps = Float.parseFloat(args[1]);
            //now we concatenate time
            h = (int) (mseconds / 3600000);
            aux = String.valueOf(h);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            m = (int) ((mseconds / 60000) % 60);
            aux = String.valueOf(m);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            s = (int) ((mseconds / 1000) % 60);
            aux = String.valueOf(s);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            f = (int) ((mseconds % 1000) * (int) fps / 1000);
            aux = String.valueOf(f);
            if (aux.length() == 1) time.append('0');
            time.append(aux);

        } else if (format.startsWith("h:m:s:f/")) {
            //this format is used in EBU's STL
            int h, m, s, f;
            float fps;
            String[] args = format.split("/");
            fps = Float.parseFloat(args[1]);
            //now we concatenate time
            h = (int) (mseconds / 3600000);
            aux = String.valueOf(h);
            //if (aux.length()==1) time.append('0');
            time.append(aux);
            time.append(':');
            m = (int) ((mseconds / 60000) % 60);
            aux = String.valueOf(m);
            //if (aux.length()==1) time.append('0');
            time.append(aux);
            time.append(':');
            s = (int) ((mseconds / 1000) % 60);
            aux = String.valueOf(s);
            //if (aux.length()==1) time.append('0');
            time.append(aux);
            time.append(':');
            f = (int) ((mseconds % 1000) * (int) fps / 1000);
            aux = String.valueOf(f);
            //if (aux.length()==1) time.append('0');
            time.append(aux);
        } else if (format.startsWith("hh:mm:ss:ff/")) {
            //this format is used in SCC
            int h, m, s, f;
            float fps;
            String[] args = format.split("/");
            fps = Float.parseFloat(args[1]);
            //now we concatenate time
            h = (int) (mseconds / 3600000);
            aux = String.valueOf(h);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            m = (int) ((mseconds / 60000) % 60);
            aux = String.valueOf(m);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            s = (int) ((mseconds / 1000) % 60);
            aux = String.valueOf(s);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
            time.append(':');
            f = (int) ((mseconds % 1000) * (int) fps / 1000);
            aux = String.valueOf(f);
            if (aux.length() == 1) time.append('0');
            time.append(aux);
        }

        return time.toString();
    }

}
