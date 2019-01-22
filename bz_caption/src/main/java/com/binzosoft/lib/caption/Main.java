package com.binzosoft.lib.caption;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Invalid arguments.");
        }
        String filePath = args[0];
        String formatTo = args[1];
        System.out.println(String.format("Convert file(%s) to %s.", filePath, formatTo));

        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        if (!filePath.contains(".")) {
            System.out.println("Undetected extension.");
            return;
        }

        String formatFrom = filePath.substring(filePath.lastIndexOf("."));
        SubtitleInterface subtitleInterface;
        if (formatFrom.equalsIgnoreCase(".srt")) {
            subtitleInterface = new FormatSRT();
        } else if (formatFrom.equalsIgnoreCase(".lrc")) {
            subtitleInterface = new FormatLRC();
        } else {
            System.out.println("Unsupported format: " + formatTo);
            return;
        }

        String outputFilePath = filePath.substring(0, filePath.lastIndexOf(".") + 1)
                + formatTo;
        System.out.println("Output file path: " + outputFilePath);

        TimedTextObject tto;
        try {
            tto = subtitleInterface.parse(filePath);
            System.out.println(tto.warnings);
            if (formatTo.equalsIgnoreCase("srt")) {
                tto.toSRT(outputFilePath);
            } else if (formatTo.equalsIgnoreCase("lrc")) {
                tto.toLRC(outputFilePath);
            } else {
                System.out.println("Unsupported format: " + formatTo);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FatalParsingException e) {
            e.printStackTrace();
        }
    }
}
