package com.binzosoft.lib.caption;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * This class represents the .SRT subtitle format
 * <br><br>
 * Copyright (c) 2012 J. David Requejo <br>
 * j[dot]david[dot]requejo[at] Gmail
 * <br><br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * <br><br>
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * <br><br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author J. David Requejo
 */
public class FormatSRT implements SubtitleInterface {

    private static final String PATTERN_INDEX = "[0-9]+";
    private static final String PATTERN_TIME = "[0-9:,]{12}\\s-->\\s[0-9:,]{12}";

    @Override
    public TimedTextObject parse(InputStream inputStream) {
        return null;
    }

    @Override
    public TimedTextObject parse(InputStream inputStream, Charset isCharset) {
        return parse(inputStream, Charset.defaultCharset());
    }

    @Override
    public TimedTextObject parse(String fileName) throws IOException {
        return parse(fileName, Charset.defaultCharset());
    }

    @Override
    public TimedTextObject parse(String path, Charset isCharset) throws IOException {
        //first lets load the file
        InputStreamReader in = new InputStreamReader(new FileInputStream(path), isCharset);
        BufferedReader br = new BufferedReader(in);

        TimedTextObject tto = new TimedTextObject();

        //the file name is saved
        tto.fileName = path;

        Caption caption = null;
        String text = "";
        String emptyLines = ""; //此变量为解析每条字幕内容的空行问题而设立。

        String line = br.readLine();
        line = line.replace("\uFEFF", ""); //remove BOM character
        try {
            do {
                line = line.trim();
                if (Pattern.matches(PATTERN_INDEX, line)) {
                    if (caption != null) {
                        caption.setContent(text);
                        tto.addCaption(caption);
                    }
                    caption = new Caption();
                    text = "";
                    emptyLines = "";
                } else if (Pattern.matches(PATTERN_TIME, line)) {
                    String startTime = line.substring(0, 12);
                    String endTime = line.substring(line.length() - 12, line.length());
                    long time = TimeUtil.valueOf(TimeUtil.FORMAT_HH_MM_SS_MMM, startTime);
                    caption.setStart(time);
                    time = TimeUtil.valueOf(TimeUtil.FORMAT_HH_MM_SS_MMM, endTime);
                    caption.setEnd(time);
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
            if (caption != null) {
                // 将最后一条字幕添加到列表
                caption.setContent(text);
                tto.addCaption(caption);
            }

            tto.sortCaptions();
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

        tto.built = true;
        return tto;
    }

    @Override
    public String[] toFile(TimedTextObject tto) {

        //first we check if the TimedTextObject had been built, otherwise...
        if (!tto.built)
            return null;

        //we will write the lines in an ArrayList,
        int index = 0;
        //the minimum size of the file is 4*number of captions, so we'll take some extra space.
        ArrayList<String> file = new ArrayList<>(5 * tto.captions.size());
        //we iterate over our captions collection, they are ordered since they come from a TreeMap
//        Collection<Caption> c = tto.captions.();
//        Iterator<Caption> itr = Collections;
        int captionNumber = 1;

        for (Caption caption : tto.captions) {
            //new caption
            //Caption current = itr.next();
            //number is written
            file.add(index++, Integer.toString(captionNumber++));
            //we check for offset value:
            if (tto.offset != 0) {
                caption.start += tto.offset;
                caption.end += tto.offset;
            }
            //time is written
            file.add(index++, TimeUtil.format(TimeUtil.FORMAT_HH_MM_SS_MMM, caption.start) +
                    " --> " + TimeUtil.format(TimeUtil.FORMAT_HH_MM_SS_MMM, caption.end));
            //offset is undone
            if (tto.offset != 0) {
                caption.start -= tto.offset;
                caption.end -= tto.offset;
            }
            //text is added
            String[] lines = cleanTextForSRT(caption);
            int i = 0;
            while (i < lines.length)
                file.add(index++, "" + lines[i++]);
            //we add the next blank line
            file.add(index++, "");
        }

        String[] toReturn = new String[file.size()];
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = file.get(i);
        }
        return toReturn;
    }

    @Override
    public Caption getCaption(long time) {
        return null;
    }

    /* PRIVATE METHODS */

    /**
     * This method cleans caption.content of XML and parses line breaks.
     */
    private String[] cleanTextForSRT(Caption current) {
        String[] lines;
        String text = current.content;
        //add line breaks
        lines = text.split("<br>");
        //clean XML
        for (int i = 0; i < lines.length; i++) {
            //this will destroy all remaining XML tags
            lines[i] = lines[i].replaceAll("\\<.*?\\>", "");
        }
        return lines;
    }

}
