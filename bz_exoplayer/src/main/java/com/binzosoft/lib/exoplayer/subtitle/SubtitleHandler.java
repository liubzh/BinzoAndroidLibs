package com.binzosoft.lib.exoplayer.subtitle;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.google.android.exoplayer2.Player;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SubtitleHandler extends Handler {

    private final String TAG = getClass().getSimpleName();

    public static final int MSG_UPDATE_SUBTITLE = 100;
    public static final int MSG_SINGLE_SENTENCE_PAUSE = 101;

    public static final int UPDATE_INTERVAL = 400; // 刷新间隔

    private Context mContext;
    private Player mPlayer;
    private ArrayList<SubtitleLoader> subtitleLoaders = new ArrayList<>();

    private boolean singleSentencePause = true;

    public SubtitleHandler(Context context) {
        this.mContext = context;
    }

    public void bindPlayer(Player player) {
        this.mPlayer = player;
    }

    public void bindSrt(TextView textView, String srtPath) throws IOException {
        bindSrt(textView, new FileInputStream(srtPath));
    }

    public void bindSrt(TextView textView, InputStream inputStream) {
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
            SubtitleLoader loader = new SubtitleLoader(textView, inputStream, "UTF-8");
            subtitleLoaders.add(loader);
            loader.start();
        }
    }

    public void setSingleSentencePause(boolean pause) {
        this.singleSentencePause = pause;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_SUBTITLE:
                Log.i(TAG, "MSG_UPDATE_SUBTITLE");
                updateOnce();
                sendEmptyMessageDelayed(MSG_UPDATE_SUBTITLE, UPDATE_INTERVAL);
                break;
            case MSG_SINGLE_SENTENCE_PAUSE:
                removeMessages(MSG_SINGLE_SENTENCE_PAUSE);
                mPlayer.setPlayWhenReady(false);
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

    /*
    public void doUpdateOnce() {
        if (subtitleList == null) {
            return;
        }
        if (mPlayer == null) {
            return;
        }
        long currentPosition = mPlayer.getCurrentPosition();
        if (subtitleList.size() == 0) {
            textView.setText("");
        }

        for (int i = 0; i < subtitleList.size(); i++) {
            SubtitleInfo srtbean = subtitleList.get(i);
            if (currentPosition > srtbean.getBeginTime() && currentPosition < srtbean.getEndTime()) {
                textView.setText(Html.fromHtml(srtbean.getSrtBody()));
                //System.out.println("subtile" + srtbean.getSrtBody());
                return;
            }
        }
        textView.setText("");
    }

    private static<T extends Comparable<T>> int bisectSearch(ArrayList<T> , int low, int high, T key) {
        if(low <= high) {
            int mid = low + ((high -low) >> 1);
            if(key.compareTo(x[mid]) == 0) {
                return mid;
            }
            else if(key.compareTo(x[mid]) < 0) {
                return bisectSearch(x, low, mid - 1, key);
            }
            else {
                return bisectSearch(x, mid + 1, high, key);
            }
        }
        return -1;
    }

     * 通过当前播放位置，找出对应的那条字幕的索引值
     * @return 对应字幕，如果没有找到字幕则返回null

    public SubtitleInfo targetSubtitle(long currentPosition) {
        if (subtitleList == null) {
            return null;
        }
        int count = subtitleList.size();
        if (count == 0) {
            return null;
        }

        for (int i = 0; i < subtitleList.size(); i++) {
            SubtitleInfo srtbean = subtitleList.get(i);
            if (currentPosition > srtbean.getBeginTime() && currentPosition < srtbean.getEndTime()) {
                return srtbean;
            }
        }
    }*/

    public void startUpdating() {
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

        public ArrayList<SubtitleInfo> getList() {
            return subtitleList;
        }

        public SubtitleLoader(TextView textView, InputStream inputStream) {
            this(textView, inputStream, "UTF-8");
        }

        public SubtitleLoader(TextView textView, InputStream inputStream, String charset) {
            this.textView = textView;
            this.inputStream = inputStream;
            this.charset = charset;
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

        public void update() {
            if (subtitleList == null) {
                return;
            }
            if (mPlayer == null) {
                return;
            }
            long currentPosition = mPlayer.getCurrentPosition();
            if (subtitleList.size() == 0) {
                textView.setText("");
            }

            for (int i = 0; i < subtitleList.size(); i++) {
                SubtitleInfo srtbean = subtitleList.get(i);
                if (currentPosition > srtbean.getBeginTime() && currentPosition < srtbean.getEndTime()) {
                    textView.setText(Html.fromHtml(srtbean.getSrtBody()));
                    //System.out.println("subtile" + srtbean.getSrtBody());
                    if (singleSentencePause && !hasMessages(MSG_SINGLE_SENTENCE_PAUSE)) {
                        long pauseDelay = srtbean.getEndTime() - currentPosition;
                        if (pauseDelay > 50) {
                            sendEmptyMessageDelayed(MSG_SINGLE_SENTENCE_PAUSE, pauseDelay);
                        }
                    }
                    return;
                }
            }
            textView.setText("");
        }

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