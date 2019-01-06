package com.binzosoft.lib.caption;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class TimeUtilTest {

    private TreeMap<String, Long> FORMATTED_TO_VALUE = new TreeMap<String, Long>() {{
        put("01:03:09,005", 3789005l);
        put("01:03:09,040", 3789040l);
        put("01:03:09,05", 3789050l);
    }};

    private TreeMap<Long, String> VALUE_TO_FORMATTED = new TreeMap<Long, String>() {{
        put(3789005l, "01:03:09,005");
        put(3789040l, "01:03:09,040");
        put(3789050l, "01:03:09,050");
    }};

    @Test
    public void valueOf() {
        for (String formattedTime : FORMATTED_TO_VALUE.keySet()) {
            long mSeconds = TimeUtil.valueOf(TimeUtil.FORMAT_HH_MM_SS_MMM, formattedTime);
            System.out.println(mSeconds);
            assertTrue(mSeconds == FORMATTED_TO_VALUE.get(formattedTime));
        }
    }

    @Test
    public void format() {
        for (long mSeconds : VALUE_TO_FORMATTED.keySet()) {
            String formattedTime = TimeUtil.format(TimeUtil.FORMAT_HH_MM_SS_MMM, mSeconds);
            System.out.println(formattedTime);
            assertTrue(formattedTime.equals(VALUE_TO_FORMATTED.get(mSeconds)));
        }
    }
}