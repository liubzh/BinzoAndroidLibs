package com.binzosoft.lib.exoplayer.subtitle;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.google.android.exoplayer2.Player;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SubtitleHandler extends Handler {

    private final String TAG = getClass().getSimpleName();

    public static final int MSG_UPDATE_SUBTITLE = 100;

    public static final int UPDATE_INTERVAL = 1000; // 刷新间隔

    private Context mContext;
    private Player mPlayer;
    private ParserSRTUtil parserSRTUtil;
    private TextView textView;
    private boolean parseFinished;

    public SubtitleHandler(Context context, TextView textView, Player player) {
        this.mContext = context;
        this.textView = textView;
        this.mPlayer = player;
        parseFinished = false;
        parserSRTUtil = ParserSRTUtil.getInstance();
    }

    public void loadSRT(String srtPath) throws IOException {
        loadSRT(new FileInputStream(srtPath));
    }

    public void loadSRT(InputStream inputStream) {
        new ParseThread(inputStream).start();
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
        parserSRTUtil.showSRT(mPlayer.getCurrentPosition(), textView);
    }

    public void startUpdating() {
        sendEmptyMessage(MSG_UPDATE_SUBTITLE);
    }

    public void stopUpdating() {
        removeMessages(MSG_UPDATE_SUBTITLE);
    }

    class ParseThread extends Thread {
        private InputStream inputStream;
        public ParseThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        @Override
        public void run() {
            // 字幕。ExoPlayer本身可以配置字幕文件并显示。这里是单独实现的字幕逻辑。
            parserSRTUtil.loadInitSRT(inputStream);
            parseFinished = true;
        }
    }
}