package com.example.bz_video_player_demo.subtitle;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SubtitleHandler extends Handler {

    private final String TAG = getClass().getSimpleName();

    public static final int TYPE_MAIN = 0; //字幕类型，主，逻辑控制以这个字幕为主
    public static final int TYPE_SECONDARY = 1; // 字母类型：次，只刷新显示，不参与逻辑判断

    public static final int MSG_UPDATE_SUBTITLE = 1000;
    public static final int MSG_SINGLE_SENTENCE_PAUSE = 1001;
    public static final int MSG_SINGLE_SENTENCE_LOOP = 1002;

    public static final int UPDATE_INTERVAL = 300; // 刷新间隔，毫秒

    /**
     * 因为视频的关键帧问题，造成无法精确seek到指定位置，这里允许存在一定的误差
     * 经过实验，关键帧设置为5的情况下，500毫秒相对好一些
      */
    //private final int SEEK_ROUGHT_TIME = 500;
    //private final int MIN_SEEK_STEP = 1000; // 毫秒

    public enum PlayMode {
        NORMAL,
        LOOP_SINGLE_SENTENCE,
        PAUSE_SINGLE_SENTENCE
    }

    private Context mContext;
    private VideoView mPlayer;
    private ArrayList<SubtitleLoader> subtitleLoaders = new ArrayList<>();
    private PlayMode playMode;

    public SubtitleHandler(Context context) {
        this.mContext = context;
    }

    public void bindPlayer(VideoView player) {
        this.mPlayer = player;
    }

    public void bindSrt(int type, TextView textView, String srtPath) throws IOException {
        bindSrt(type, textView, new FileInputStream(srtPath));
    }

    public void bindSrt(int type, TextView textView, InputStream inputStream) {
        if (textView == null) {
            return;
        } else if (inputStream == null) {
            return;
        }
        synchronized (subtitleLoaders) {
            for (SubtitleLoader loader : subtitleLoaders) {
                if (textView == loader.textView) {
                    subtitleLoaders.remove(loader);
                }
            }
            SubtitleLoader loader = new SubtitleLoader(type, textView, inputStream, "UTF-8");
            subtitleLoaders.add(loader);
            loader.start();
        }
    }

    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_SUBTITLE:
                //Log.i(TAG, "MSG_UPDATE_SUBTITLE");
                //updateOnce();
                targetSubtitle(mPlayer.getCurrentPosition());
                sendEmptyMessageDelayed(MSG_UPDATE_SUBTITLE, UPDATE_INTERVAL);
                break;
            case MSG_SINGLE_SENTENCE_PAUSE:
                removeMessages(MSG_SINGLE_SENTENCE_PAUSE);
                mPlayer.pause();
                for (SubtitleLoader loader : subtitleLoaders) {
                    if (loader.isMainSubtitle()) {
                        int position = loader.getCurrentSubtitle().getEndTime();
                        loader.targetSubtitle(position);
                    }
                }
                break;
            case MSG_SINGLE_SENTENCE_LOOP:
                removeMessages(MSG_SINGLE_SENTENCE_LOOP);
                for (SubtitleLoader loader : subtitleLoaders) {
                    if (loader.isMainSubtitle()) {
                        long position = loader.getCurrentSubtitle().getBeginTime();
                        mPlayer.seekTo((int) position);
                        loader.targetSubtitle(position);
                    }
                }
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

    public void updateOnce() {
        for (SubtitleLoader loader : subtitleLoaders) {
            loader.update();
        }
    }

    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        stopUpdating();
    }

    public void play() {
        mPlayer.start();
        startToUpdate();
    }

    public void previous() {
        pause();
        int position = mPlayer.getCurrentPosition();
        for (SubtitleLoader loader : subtitleLoaders) {
            if (loader.isMainSubtitle()) {
                SubtitleInfo previousCaption = loader.getPreviousSubtitle(position);
                Log.i(TAG, "previousCaption:" + previousCaption);
                if (previousCaption != null) {
                    int seekTo = previousCaption.getBeginTime();
                    //int roughTime = preciseTime / SEEK_ROUGHT_TIME * SEEK_ROUGHT_TIME;
                    Log.i(TAG, "seekTo:" + seekTo);
                    mPlayer.seekTo(seekTo);
                    targetSubtitle(seekTo + 500);
                }
            }
        }
    }

    public void next() {
        pause();
        int position = mPlayer.getCurrentPosition();
        for (SubtitleLoader loader : subtitleLoaders) {
            if (loader.isMainSubtitle()) {
                SubtitleInfo nextCaption = loader.getNextSubtitle(position);
                Log.i(TAG, "nextCaption:" + nextCaption);
                if (nextCaption != null) {
                    int seekTo = nextCaption.getBeginTime();
//                    if (seekTo - position < MIN_SEEK_STEP) {
//                        seekTo = position + 500;
//                    }
                    mPlayer.seekTo(seekTo);
                    targetSubtitle(seekTo + 500);
                }
            }
        }
    }

    public void targetSubtitle(int position) {
        for (SubtitleLoader loader : subtitleLoaders) {
            if (loader.isMainSubtitle()) {
                loader.targetSubtitle(position);
            }
        }
    }

    public void startToUpdate() {
        sendEmptyMessage(MSG_UPDATE_SUBTITLE);
    }

    public void stopUpdating() {
        removeMessages(MSG_UPDATE_SUBTITLE);
    }

    public void destroy() {
        for (SubtitleLoader loader : subtitleLoaders) {
            loader.destroy();
        }
    }

    public class SubtitleLoader extends Thread {

        private final String TAG = getClass().getSimpleName();

        private volatile boolean parseFinished;
        private ArrayList<SubtitleInfo> subtitleList;
        private InputStream inputStream;
        private String charset;
        private TextView textView;
        private long currentPosition;
        private int subtitleIndex = -1;
        private int type = TYPE_MAIN;

        public ArrayList<SubtitleInfo> getList() {
            return subtitleList;
        }

        public SubtitleLoader(int type, TextView textView, InputStream inputStream) {
            this(type, textView, inputStream, "UTF-8");
        }

        public SubtitleLoader(int type, TextView textView, InputStream inputStream, String charset) {
            this.type = type;
            this.textView = textView;
            this.inputStream = inputStream;
            this.charset = charset;
        }

        public void reset() {
            subtitleIndex = -1;
        }

        public boolean isMainSubtitle() {
            return type == TYPE_MAIN;
        }

        public SubtitleInfo getCurrentSubtitle() {
            if (subtitleList == null || mPlayer == null) {
                return null;
            }
            final int targetIndex = subtitleIndex;
            if (targetIndex >= 0 && targetIndex < subtitleList.size()) {
                return subtitleList.get(targetIndex);
            } else {
                return null;
            }
        }

        public SubtitleInfo getPreviousSubtitle(int position) {
            if (subtitleList == null || mPlayer == null) {
                return null;
            }

            SubtitleInfo previous = null;

            for (int i = 0; i < subtitleList.size(); i++) {
                SubtitleInfo bean = subtitleList.get(i);
                if (bean.getBeginTime() < position) {
                    if (previous == null || bean.getBeginTime() > previous.getBeginTime()) {
                        previous = bean;
                    }
                }
            }

            Log.i(TAG, "position:" + position);
            if (previous != null) {
                Log.i(TAG, "previous:" + previous.getBeginTime());
            }
            return previous;
        }

        public SubtitleInfo getNextSubtitle(int position) {
            if (subtitleList == null || mPlayer == null) {
                return null;
            }

            SubtitleInfo next = null;

            for (int i = 0; i < subtitleList.size(); i++) {
                SubtitleInfo bean = subtitleList.get(i);
                if (bean.getBeginTime() > position) {
                    if (next == null || bean.getBeginTime() < next.getBeginTime()) {
                        Log.i(TAG, "target index: " + i);
                        next = bean;
                    }
                }
            }

            Log.i(TAG, "position:" + position);
            if (next != null) {
                Log.i(TAG, "next:" + next.getBeginTime());
            }
            return next;
        }

        public void destroy() {
            parseFinished = true; // 结束解析srt的当前线程
            subtitleList.clear();
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void targetSubtitle(long position) {
            if (subtitleList == null || mPlayer == null) {
                return;
            }

            if (subtitleList.size() == 0) {
                textView.setText("");
                return;
            }

            // 重新定位字幕. BEGIN
            int index = -1;
            for (int i = 0; i < subtitleList.size(); i++) {
                SubtitleInfo bean = subtitleList.get(i);
                if (position > bean.getBeginTime() && position < bean.getEndTime()) {
                    index = i;
                    break;
                }
            }
            // 重新定位字幕. END

            // 更新字幕显示. BEGIN
            if (index == -1) {
                if (!TextUtils.isEmpty(textView.getText())) {
                    // 当前时间点没有匹配到要显示的字幕
                    textView.setText("");
                }
            } else {
                // 更新下一条要显示的字幕
                textView.setText(Html.fromHtml(subtitleList.get(index).getSrtBody()));
                subtitleIndex = index;
            }
            // 更新字幕显示. END
        }

        public void update() {
            if (subtitleList == null || mPlayer == null) {
                return;
            }
            currentPosition = mPlayer.getCurrentPosition();
            if (subtitleList.size() == 0) {
                textView.setText("");
            }

            /*
            if (subtitleIndex > 0) {
                if (isMainSubtitle()) {
                    // 判断播放模式下的播放进度控制
                    judgePlayMode();
                }
                SubtitleInfo subtitle = subtitleList.get(subtitleIndex);
                if (currentPosition >= subtitle.getBeginTime() && currentPosition <= subtitle.getEndTime()) {
                    // 字幕未发生变化，直接返回
                    return;
                }
            }
            */

            targetSubtitle(currentPosition); // 刷新字幕
        }

        /*
        // 根据播放模式控制播放器.
        private void judgePlayMode() {
            if (type != TYPE_MAIN) {
                return;
            }
            SubtitleInfo info = subtitleList.get(subtitleIndex);
            if (playMode == PlayMode.PAUSE_SINGLE_SENTENCE && !hasMessages(MSG_SINGLE_SENTENCE_PAUSE)) {
                if (currentPosition >= info.getEndTime()) {
                    stopUpdating();
                    mPlayer.pause();
                    targetSubtitle(info.getEndTime());
                }
            } else if (playMode == PlayMode.LOOP_SINGLE_SENTENCE) {
                if (currentPosition >= info.getEndTime()) {
                    mPlayer.seekTo(info.getBeginTime() & 1000);
                    targetSubtitle(info.getBeginTime() & 1000);
                    if (mPlayer.isPlaying()) {
                        startToUpdate();
                    }
                }
            } else {
                removeMessages(MSG_SINGLE_SENTENCE_LOOP);
                removeMessages(MSG_SINGLE_SENTENCE_PAUSE);
            }
        }
        */

        @Override
        public void run() {
            // 字幕。ExoPlayer本身可以配置字幕文件并显示。这里是单独实现的字幕逻辑。
            StringBuffer sb;
            BufferedReader br = null;
            StringBuffer srtBody_1;
            try {
                subtitleList = new ArrayList();
                br = new BufferedReader(new InputStreamReader(inputStream, charset));
                String line;

                sb = new StringBuffer();
                srtBody_1 = new StringBuffer();
                while (!parseFinished && (line = br.readLine()) != null) {
                    if (!line.equals("")) {
                        sb.append(line).append("@");
                        continue;
                    }

                    String[] parseStrs = sb.toString().split("@");
                    if (parseStrs.length < 3) {
                        sb.delete(0, sb.length());
                        continue;
                    }
                    SubtitleInfo srt = new SubtitleInfo();
                    // 解析开始和结束时间
                    String timeTotime = parseStrs[1];
                    int begin_hour = Integer.parseInt(timeTotime.substring(0, 2));
                    int begin_mintue = Integer.parseInt(timeTotime.substring(3, 5));
                    int begin_scend = Integer.parseInt(timeTotime.substring(6, 8));
                    int begin_milli = Integer.parseInt(timeTotime.substring(9, 12));
                    int beginTime = (begin_hour * 3600 + begin_mintue * 60 + begin_scend) * 1000 + begin_milli;
                    int end_hour = Integer.parseInt(timeTotime.substring(17, 19));
                    int end_mintue = Integer.parseInt(timeTotime.substring(20, 22));
                    int end_scend = Integer.parseInt(timeTotime.substring(23, 25));
                    int end_milli = Integer.parseInt(timeTotime.substring(26, 29));
                    int endTime = (end_hour * 3600 + end_mintue * 60 + end_scend) * 1000 + end_milli;

                    for (int i = 2; i < parseStrs.length; i++) {
                        if (i < parseStrs.length - 1) {
                            srtBody_1.append(parseStrs[i] + "<br>");
                        } else {
                            srtBody_1.append(parseStrs[i]);
                        }
                    }

                    srt.setBeginTime(beginTime);
                    srt.setEndTime(endTime);
                    srt.setSrtBody(srtBody_1.toString());
                    subtitleList.add(srt);

                    srtBody_1.delete(0, srtBody_1.length());
                    sb.delete(0, sb.length());
                }
                Log.i(TAG, "subtitleList.size()=" + subtitleList.size());
                parseFinished = true; //标记解析完成，可界面显示了
                if (textView != null) {
                    sendEmptyMessage(MSG_UPDATE_SUBTITLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}