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

    public static final int UPDATE_INTERVAL = 1000; // 刷新间隔

    private Context mContext;
    private Player mPlayer;
    private TextView textView;
    private volatile boolean parseFinished;
    private ArrayList<SubtitleInfo> subtitleList;

    public SubtitleHandler(Context context) {
        this.mContext = context;
        parseFinished = false;
    }

    public void bindPlayer(Player player) {
        this.mPlayer = player;
    }

    public void bindTextView(TextView textView) {
        this.textView = textView;
    }

    public void loadSrt(String srtPath) throws IOException {
        loadSrt(new FileInputStream(srtPath));
    }

    public void loadSrt(InputStream inputStream) {
        parseFinished = false;
        new ParseSrtThread(inputStream, "UTF-8").start();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_SUBTITLE:
                Log.i(TAG, "MSG_UPDATE_SUBTITLE");
                if (!parseFinished) {
                    // 未初始化完成，等待
                    Log.i(TAG, "waiting for initializing finishes");
                    return;
                }
                doUpdateOnce();
                sendEmptyMessageDelayed(MSG_UPDATE_SUBTITLE, UPDATE_INTERVAL);
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

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

    public void startUpdating() {
        sendEmptyMessage(MSG_UPDATE_SUBTITLE);
    }

    public void stopUpdating() {
        removeMessages(MSG_UPDATE_SUBTITLE);
    }

    public void destroy() {
        parseFinished = true; // 结束解析src的线程
    }

    class ParseSrtThread extends Thread {
        private InputStream inputStream;
        private String charset;

        public ParseSrtThread(InputStream inputStream) {
            this(inputStream, "UTF-8");
        }

        public ParseSrtThread(InputStream inputStream, String charset) {
            this.inputStream = inputStream;
            this.charset = charset;
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