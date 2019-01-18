package com.binzosoft.lib.caption;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public class FormatLRC implements SubtitleInterface {

    private static final String PATTERN_TIME = "\\[[0-9]{2,3}:[0-9]{2}.[0-9]{2}\\].*";
    private static final String DEFAULT_CHARSET = "UTF-8";

    @Override
    public TimedTextObject parse(String filePath) throws IOException {
        return parse(filePath, Charset.forName(DEFAULT_CHARSET));
    }

    @Override
    public TimedTextObject parse(String filePath, Charset charset) throws IOException {
        //first lets load the file
        InputStreamReader in = new InputStreamReader(new FileInputStream(filePath), charset);
        BufferedReader br = new BufferedReader(in);

        TimedTextObject tto = new TimedTextObject();

        //the file name is saved
        tto.fileName = filePath;

        Caption captionLast = null;
        Caption caption = null;
        String text = "";
        String emptyLines = ""; //此变量为解析每条字幕内容的空行问题而设立。

        String line = br.readLine();
        line = line.replace("\uFEFF", ""); //remove BOM character
        try {
            do {
                line = line.trim();
                if (Pattern.matches(PATTERN_TIME, line)) {
                    String time = line.substring(1, 9);
                    System.out.println(time);
                    break;
                    /*
                    if (line.length() > 10) {
                        // [00:04.37]词：琼瑶
                        // 字符个数大于10，说明有内容
                    }
                    captionLast = caption;
                    if (caption != null) {
                        caption.setContent(text);
                        tto.addCaption(caption);
                    }
                    caption = new Caption();
                    text = "";
                    emptyLines = "";
                    */
                } else if (line.isEmpty()) {
                    emptyLines = emptyLines + "<br>"; //多行以 HTML 换行标签分隔
                } else {
//                    if (null == text || text.isEmpty()) {
//                        text = line;
//                    } else {
//                        text = text + "<br>" + emptyLines + line;
//                        emptyLines = "";
//                    }
                }
            } while ((line = br.readLine()) != null);
            if (caption != null) {
                // 将最后一条字幕添加到列表
                caption.setContent(text);
                tto.addCaption(caption);
            }

            tto.sortCaptions(); //根据时间进行排序
            tto.built = true;
        } catch (NullPointerException e) {
            tto.warnings += "unexpected end of file, maybe last caption is not complete.\n\n";
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
            try {
                //we close the reader
                in.close();
            } catch (Exception e) {
            }
        }
        return tto;
    }

    @Override
    public void toFile(TimedTextObject tto, String filePath) throws IOException {

        if (!tto.built) {
            throw new IllegalStateException("TimedTextObject has not been built.");
        }

        OutputStreamWriter writer = null;

        try {
            writer = new OutputStreamWriter(
                    new FileOutputStream(filePath), "utf-8");

            int index = 1;

            for (Caption caption : tto.captions) {
                writer.write(String.valueOf(index));
                writer.write("\n");
                String startTime = TimeUtil.format(TimeUtil.FORMAT_HH_MM_SS_MMM, caption.start);
                String endTime = TimeUtil.format(TimeUtil.FORMAT_HH_MM_SS_MMM, caption.end);
                writer.write(String.format("%s --> %s", startTime, endTime));
                writer.write("\n");
                writer.write(caption.content.replace("<br>", "\n"));
                writer.write("\n\n");
                index++;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public Caption getCaption(long time) {
        return null;
    }
}
