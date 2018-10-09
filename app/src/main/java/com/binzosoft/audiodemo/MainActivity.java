package com.binzosoft.audiodemo;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.binzosoft.audio.AudioTrackPlayer;
import com.binzosoft.audio.model.Lyric;
import com.binzosoft.audio.model.LyricItem;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {

    private AudioTrackPlayer player;
    private TextView tv;
    private Button bt1, bt2;
    private Button btPrev, btNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.text);
        bt1 = findViewById(R.id.button1);
        bt1.setOnClickListener(this);
        bt2 = findViewById(R.id.button2);
        bt2.setOnClickListener(this);
        btPrev = findViewById(R.id.button_previous);
        btPrev.setOnClickListener(this);
        btNext = findViewById(R.id.button_next);
        btNext.setOnClickListener(this);

        //MediaPlayer player = new MediaPlayer();

        player = new AudioTrackPlayer();
        player.setOnLyricUpdateListener(lyricUpdateListener);
        player.setPlayMode(AudioTrackPlayer.PLAY_MODE.LOOP_SINGLE_SONG); // 设置播放模式为单曲循环

        requestPermission(Permission.READ_EXTERNAL_STORAGE);
        try {
            // 设置音频数据
            player.setAudioData(getAssets().open("Lesson01.wav"));
        } catch (Exception e) {
            e.printStackTrace();
            player = null;
        }
        try {
            // 设置歌词数据
            player.setLyricData(getAssets().open("Lesson01.lrc"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        try {
            if (id == R.id.button1) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            } else if (id == R.id.button2) {
                player.seekTo(121400);
            } else if (id == R.id.button_previous) {
                player.seekToPrevious();
            } else if (id == R.id.button_next) {
                player.seekToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioTrackPlayer.OnLyricUpdateListener lyricUpdateListener = new AudioTrackPlayer.OnLyricUpdateListener() {
        @Override
        public void onLyricUpdate(Lyric lyric) {
            LyricItem lyricItem = lyric.getCurrentItem();
            if (lyric == null || lyricItem == null) {
                tv.setText("");
            } else {
                tv.setText(lyricItem.getContent());
            }
        }
    };

    public class Permission {
        public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    private boolean needsToRequestPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int granted = ContextCompat.checkSelfPermission(this, permission);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, 0x0010);
    }

    private void requestPermission(String permission) {
        if (needsToRequestPermission(permission)) {
            requestPermissions(new String[]{permission});
        }
    }
}
