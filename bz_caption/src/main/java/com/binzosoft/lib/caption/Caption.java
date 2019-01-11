package com.binzosoft.lib.caption;

public class Caption {

    public long start;
    public long end;

    public String content = "";

    @Override
    public String toString() {
        return String.format("Caption { %s->%s: %s }", start, end, content);
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
