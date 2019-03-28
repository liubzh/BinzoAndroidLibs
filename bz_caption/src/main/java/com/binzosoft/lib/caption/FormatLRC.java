package com.binzosoft.lib.caption;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FormatLRC implements SubtitleInterface {

    private static final String PATTERN_TIME = "\\[[0-9]{2,3}:[0-9]{2}.[0-9]{2}\\].*";
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final boolean DEBUG = true;

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

        Caption caption;
        String text = "";
        String emptyLines = ""; //此变量为解析每条字幕内容的空行问题而设立。

        String line = br.readLine();
        line = line.replace("\uFEFF", ""); //remove BOM character
        try {
            do {
                line = line.trim();
                if (Pattern.matches(PATTERN_TIME, line)) {
                    String time = line.substring(1, 9);
                    if (DEBUG) System.out.println("\"" + time + "\" is in []");
                    if (line.length() > 10) {
                        // [00:04.37]词：琼瑶
                        // 字符个数大于10，说明有内容
                        text = line.substring(10);
                    } else {
                        text = "";
                    }
                    caption = new Caption();
                    caption.setContent(text);
                    long startTime = TimeUtil.valueOf(TimeUtil.FORMAT_MM_SS_MM, time);
                    if (DEBUG) System.out.println("startTime: " + startTime);
                    long endTime = startTime + 1; //暂时设定为假的结束时间，解析完成之后，进行真实结束时间的设定
                    caption.setStart(startTime);
                    caption.setEnd(endTime);
                    tto.addCaption(caption);
                    text = "";
                    emptyLines = "";
                } else if (line.startsWith("[")) {
                    // [ti:] [ar:] [al:] 等标签暂时忽略
                } else if (line.isEmpty()) {
                    emptyLines = emptyLines + "<br>"; //多行以 HTML 换行标签分隔
                } else {
                    if (null == text || text.isEmpty()) {
                        text = line;
                    } else {
                        text = text + "<br>" + emptyLines + line;
                        emptyLines = "";
                    }
                }
            } while ((line = br.readLine()) != null);
            ArrayList<Caption> captions = tto.getCaptions();

            tto.sortCaptions(); //根据时间进行排序
            for (int i = 0; i < captions.size(); i++) {
                Caption c = captions.get(i);
                if (i == captions.size() - 1) {
                    // 最后一条
                    c.setEnd(c.getStart() + 300000); // 时长设置为5分钟
                } else {
                    long end = captions.get(i + 1).getStart() - 1;
                    c.setEnd(end);
                }
            }
            tto.built = true;
        } catch (NullPointerException e) {
            tto.warnings += "unexpected end of file, maybe last caption is not complete.\n\n";
        } finally {
            try {
                br.close();
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

            for (int i = 0; i < tto.captions.size(); i++) {
                Caption caption = tto.captions.get(i);
                if (i != 0) {
                    // 条目之间的换行
                    writer.write("\n");
                }
                String startTime = TimeUtil.format(TimeUtil.FORMAT_MM_SS_MM, caption.getStart());
                writer.write(String.format("[%s]", startTime));
                writer.write(caption.content);
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
