package com.binzosoft.lib.audio.model;

public class LyricItem {

    private String content;
    private int timestamp;
    private int index = -1;

    public LyricItem(int timestamp, String content) {
        this.timestamp = timestamp;
        this.content = content;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getContent() {
        return content;
    }

    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LyricItem {")
                .append(index)
                .append(", " + timestamp + "ms")
                .append(", " + content)
                .append("}");
        return sb.toString();
    }
}
