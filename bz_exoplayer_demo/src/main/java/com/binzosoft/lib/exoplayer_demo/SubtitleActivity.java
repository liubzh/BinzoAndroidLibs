package com.binzosoft.lib.exoplayer_demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.binzosoft.lib.exoplayer.subtitle.Caption;
import com.binzosoft.lib.exoplayer.subtitle.FormatSRT;
import com.binzosoft.lib.exoplayer.subtitle.Time;
import com.binzosoft.lib.exoplayer.subtitle.TimedTextObject;
import com.binzosoft.lib.util.DateTimeUtil;

import java.io.IOException;

public class SubtitleActivity extends AppCompatActivity implements Button.OnClickListener{

    private final String TAG = "SubtitleActivity";

    private TextView textView;
    private TextView time;

    private final int UPDATE_INTERVAL = 1000; //刷新间隔，单位：毫秒

    private final int MSG_TIMER_TICK = 1000;
    private final int MSG_TIMER_START = 1001;
    private final int MSG_TIMER_STOP = 1002;
    private final int MSG_CAPTION_CLEAR = 1003;
    private final int MSG_CAPTION_CHANGE = 1004;

    private long startTime = 0;
    private long elapsedTime = 0;
    private long duration = 0;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Timer. begin 模拟一个播放计时器，模拟播放时长变化
                case MSG_TIMER_START:
                    Log.i(TAG, "MSG_TIMER_START");
                    if (duration <= 0) {
                        throw new IllegalArgumentException("duration has not been set");
                    }
                    startTime =  SystemClock.elapsedRealtime();
                    sendEmptyMessage(MSG_TIMER_TICK);
                    break;
                case MSG_TIMER_STOP:
                    Log.i(TAG, "MSG_TIMER_STOP");
                    startTime = 0;
                    removeMessages(MSG_TIMER_TICK);
                    time.setText(DateTimeUtil.format(0, DateTimeUtil.HHmmss));
                    break;
                case MSG_TIMER_TICK:
                    Log.i(TAG, "MSG_TIMER_TICK");
                    sendEmptyMessageDelayed(MSG_TIMER_TICK, UPDATE_INTERVAL);
                    elapsedTime = SystemClock.elapsedRealtime() - startTime;
                    time.setText(DateTimeUtil.format(elapsedTime, DateTimeUtil.HHmmss));
                    if (elapsedTime >= duration) {
                        sendEmptyMessage(MSG_TIMER_STOP);
                    }
                    Log.i(TAG, "elapsed:" + elapsedTime);
                    break;
                // Timer. end
                case MSG_CAPTION_CLEAR:
                    Log.i(TAG, "MSG_CAPTION_CLEAR");
                    textView.setText("");
                    break;
                case MSG_CAPTION_CHANGE:
                    Log.i(TAG, "MSG_CAPTION_CHANGE");
                    removeMessages(MSG_CAPTION_CLEAR);
                    if (msg.obj instanceof Caption) {
                        Caption caption = (Caption) msg.obj;
                        textView.setText(caption.content);
                        long duration = caption.end.getMseconds() - caption.start.getMseconds();
                        sendEmptyMessageDelayed(MSG_CAPTION_CLEAR, duration);
                    } else {
                        throw new IllegalArgumentException("Object is not Caption.");
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
    private MyHandler mHandler = new MyHandler();

    private Button button1, button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitle);
        textView = findViewById(R.id.text);
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
        time = findViewById(R.id.time);
        time.setText(DateTimeUtil.format(DateTimeUtil.currentTimeMillis(), DateTimeUtil.HHmmss));

        try {
            TimedTextObject subtitle = new FormatSRT().parseFile("test",
                    getAssets().open("test.srt"));
            StringBuffer sb = new StringBuffer();
            for (Integer key : subtitle.captions.keySet()) {
                Caption caption = subtitle.captions.get(key);
                sb.append(key).append("\n")
                        .append("start:").append(caption.start).append("\n")
                        .append("end:").append(caption.end).append("\n")
                        .append("rawContent:").append(caption.rawContent).append("\n")
                        .append("content:").append(caption.content).append("\n")
                        .append("region:").append(caption.region).append("\n")
                        .append("style:").append(caption.style).append("\n");
            }
            textView.setText(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                duration = 30 * 1000; // 模拟播放时长
                mHandler.sendEmptyMessage(MSG_TIMER_START);
                break;
            case R.id.button2:
                duration = 0;
                mHandler.sendEmptyMessage(MSG_TIMER_STOP);
                break;
        }
    }
}
