package com.binzosoft.lib.caption;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class FormatSRTTest {

    @Test
    public void parseFile() {
        FormatSRT formatter = new FormatSRT();
        TimedTextObject tto = null;
        try {
            tto = formatter.parse("./src/test/assets/f2.srt");
            System.out.println(tto.warnings);
        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(false);
        }
        for (Caption caption : tto.captions) {
            System.out.println(caption.toString());
        }
        assertTrue(169 == tto.captions.size());
    }
}