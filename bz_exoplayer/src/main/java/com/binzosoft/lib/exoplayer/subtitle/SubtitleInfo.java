package com.binzosoft.lib.exoplayer.subtitle;

public class SubtitleInfo {
    private long beginTime;
    private long endTime;
    private String srtBody;

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getSrtBody() {
        return srtBody;
    }

    public void setSrtBody(String srtBody) {
        this.srtBody = srtBody;
    }

    @Override
    public String toString() {
        return "" + beginTime + ":" + endTime + ":" + srtBody;
    }
}