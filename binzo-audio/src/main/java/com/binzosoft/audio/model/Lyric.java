package com.binzosoft.audio.model;

import android.util.Log;

import java.util.ArrayList;

public class Lyric {

    private String TAG = getClass().getSimpleName();

    private ArrayList<LyricItem> items;
    private int index = -1;
    private int duration;

    public Lyric(int duration) {
        items = new ArrayList<>();
        this.duration = duration;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<LyricItem> getItems() {
        return items;
    }

    public boolean next() {
        int idx = index + 1;
        if (items.size() > 0 && idx >= 0 && idx <= items.size() - 1) {
            index++;
            return true;
        } else {
            index = -1;
            return false;
        }
    }

    public LyricItem getNextItem() {
        int idx = index + 1;
        if (items.size() > 0 && idx >= 0 && idx <= items.size() - 1) {
            return items.get(idx);
        } else {
            return null;
        }
    }

    public boolean previous() {
        int idx = index - 1;
        if (items.size() > 0 && idx >= 0 && idx <= items.size() - 1) {
            index--;
            return true;
        } else {
            index = -1;
            return false;
        }
    }

    public LyricItem getPreviousItem() {
        int idx = index - 1;
        if (items.size() > 0 && idx >= 0 && idx <= items.size() - 1) {
            return items.get(idx);
        } else {
            return null;
        }
    }

    public void addItem(LyricItem item) {
        boolean rst = items.add(item);
        if (rst) {
            item.setIndex(items.size() - 1);
        }
    }

    public LyricItem getItem(int index) {
        return items.get(index);
    }

    public int getItemCount() {
        return items.size();
    }

    public LyricItem getCurrentItem() {
        if (items.size() > 0 && index >= 0 && index < items.size()) {
            return items.get(index);
        } else {
            return null;
        }
    }

    /**
     * 这里使用二分查找法定位当前播放时间点的歌词
     * @param msec
     */
    public void target(int msec) {
        Log.i(TAG, "target(msec:" + msec + ")");
        if (msec < 0 || msec > duration) {
            // 超出范围的时间无效
            index = -1;
            return;
        } else if (msec < items.get(0).getTimestamp()) {
            // 小于第一句歌词时间戳的时间无效
            index = -1;
            return;
        }
        int low = 0, high = items.size() - 1, mid = -1;
        while (low <= high) {
            mid = (low + high) / 2;
            //Log.i(TAG, "low:" + low + ", high:" + high + ", mid:" + mid);
            int midTimestamp = items.get(mid).getTimestamp();
            int nextTimestamp = items.get(mid + 1).getTimestamp();
            //Log.i(TAG, mid + "-midTimestamp:" + midTimestamp + "; "
            //        + (mid + 1) + "-nextTimestamp:" + nextTimestamp);
            if(msec >= midTimestamp && msec < nextTimestamp) {
                // 找到目标歌词下标
                break;
            } else if(msec < midTimestamp) {
                high = mid - 1;
            } else if (msec >= nextTimestamp) {
                low = mid + 1;
            }
            if (low == high) {
                mid = low;
                break;
            }
        }
        index = mid;
        Log.i(TAG, "target index:" + index);
    }

}
