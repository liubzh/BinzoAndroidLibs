package com.binzosoft.lib.audio.model;

public class WavInfo {

    private int rate;
    private int channels;  // 2 channels is stereo; 1 channel is mono
    private int dataSize;

    public WavInfo(int rate, int channels, int dataSize) {
        this.rate = rate;
        this.channels = channels;
        this.dataSize = dataSize;
    }

    public int getRate() {
        return rate;
    }

    public boolean isStereo() {
        return channels == 2;
    }

    public boolean isMono() {
        return channels == 1;
    }

    public int getChannels() {
        return channels;
    }

    public int getDataSize() {
        return dataSize;
    }
}
