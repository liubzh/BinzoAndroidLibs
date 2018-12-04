package com.binzosoft.lib.file_manager;

public class PathStackItem {

    private String path;
    private int lastPosition = 0;
    private int lastOffset = 0;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public int getLastOffset() {
        return lastOffset;
    }

    public void setLastOffset(int lastOffset) {
        this.lastOffset = lastOffset;
    }
}
