package com.binzosoft.lib.caption;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        FormatSRT formatter = new FormatSRT();
        TimedTextObject tto = null;
        try {
            tto = formatter.parse("../../test/assets/f2.srt");
            System.out.println(tto.warnings);
            tto.toSRT("./out/f2.srt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
