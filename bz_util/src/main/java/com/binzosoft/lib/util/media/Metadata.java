package com.binzosoft.lib.util.media;

import android.media.MediaMetadataRetriever;

public class Metadata {
    private String title;
    private String album;
    private String mime;
    private String artist;
    private String duration;
    private String bitrate;
    private String date;
    private String videoHeight;
    private String videoWidth;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(String videoHeight) {
        this.videoHeight = videoHeight;
    }

    public String getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(String videoWidth) {
        this.videoWidth = videoWidth;
    }

    @Override
    public String toString() {
        String template = "{title:%s;mime%s;artist:%s;duration:%s;bitrate:%s;date:%s}";
        return String.format(template, title, mime, artist, duration, bitrate, date);
    }

    public static Metadata retrieve(String path) {
        Metadata metadata = new Metadata();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        // api level 10, 即从GB2.3.3开始有此功能
        metadata.setTitle(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        // 专辑名
        metadata.setAlbum(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        // 媒体格式
        metadata.setMime(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
        // 艺术家
        metadata.setArtist(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        // 播放时长单位为毫秒
        metadata.setDuration(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        // 从api level 14才有，即从ICS4.0才有此功能
        metadata.setBitrate(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
        // 路径
        metadata.setDate(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE));

        metadata.setVideoWidth(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        metadata.setVideoHeight(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        return metadata;
    }
}
