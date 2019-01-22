package com.binzosoft.lib.caption;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FormatLRCTest {

    @Test
    public void time() {
        String pattern = "\\[[0-9]{2,3}:[0-9]{2}.[0-9]{2,3}\\].*";

        String content = "[00:04.37]词：琼瑶";
        assertTrue(content.matches(pattern));
        content = "[000:04.370]词：琼瑶";
        assertTrue(content.matches(pattern));

        content = "[000:040.370]词：琼瑶";
        assertFalse(content.matches(pattern));
    }

    @Test
    public void parseFile() {
        FormatLRC formatter = new FormatLRC();
        TimedTextObject tto;
        try {
            tto = formatter.parse("./src/test/assets/l2.lrc");
            System.out.println(tto.warnings);
            for (Caption caption : tto.captions) {
                System.out.println(caption.toString());
            }
            tto.toSRT("./src/main/shell/out/tmp.srt");
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        assertTrue(true);
    }
}