package com.example.bz_video_player_demo.subtitle;

public class SubtitleInfo {
    private int beginTime;
    private int endTime;
    private String srtBody;

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
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