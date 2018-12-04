package com.example.bz_video_player_demo;

import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.bz_video_player_demo.subtitle.SubtitleHandler;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private VideoView videoView;
    private TextView subtitleView;
    private Button button1, button2;
    private SubtitleHandler subtitleHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subtitleView = findViewById(R.id.subtitle1);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            setSelectionActionCallback2(subtitleView);
        }


        button1 = findViewById(R.id.Button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.Button2);
        button2.setOnClickListener(this);

        videoView = findViewById(R.id.VideoView);
        videoView.setOnClickListener(this);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(0);
                videoView.start();
            }
        });
        String filePath = "/sdcard/learn/Peppa Pig/Peppa Pig S01/Peppa Pig S01E001 - Muddy Puddles.k.mp4";
        videoView.setVideoPath(filePath);

        String title = filePath.substring(filePath.lastIndexOf("/"));
        title = title.substring(0, title.lastIndexOf("."));
        if (title.contains(" - ")) {
            title = title.substring(title.indexOf(" - ") + 3);
        }
        setTitle(title);

        //videoView.setVideoPath("/sdcard/Movies/LegallyBlonde.mp4");
        //videoView.setMediaController(new MediaController(this));
        videoView.start();

        subtitleHandler = new SubtitleHandler(this);
        subtitleHandler.bindPlayer(videoView);
        //subtitleHandler.setPlayMode(SubtitleHandler.PlayMode.PAUSE_SINGLE_SENTENCE);
        try {
            subtitleHandler.bindSrt(SubtitleHandler.TYPE_MAIN, subtitleView,
                    "/sdcard/learn/Peppa Pig/Peppa Pig S01/Peppa Pig S01E001 - Muddy Puddles.srt");
                    //"/sdcard/Movies/LegallyBlonde1.En.srt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.VideoView) {
            if (videoView.isPlaying()) {
                subtitleHandler.pause();
            } else {
                subtitleHandler.play();
            }
        } else if (id == R.id.Button1) {
            subtitleHandler.previous();
        } else if (id == R.id.Button2) {
            subtitleHandler.next();
        }
    }


    private void setSelectionActionCallback2(final TextView textView) {
        ActionMode.Callback2 callback2;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            callback2 = new ActionMode.Callback2() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    /*
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menuInflater.inflate(R.menu.selection_action_google_translate, menu);
                    return true;//返回false则不会显示弹窗
                    */
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    //return false; // 同时显示自定义和系统默认复制、粘贴菜单选项
                    MenuInflater menuInflater = actionMode.getMenuInflater();
                    menu.clear(); // 清除系统默认复制、粘贴选项后，只显示自定义菜单选项
                    menuInflater.inflate(R.menu.selection_action_google_translate, menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    if (textView == null || !textView.hasSelection()) {
                        return false;
                    }
                    String selection = textView.getText()
                            .subSequence(textView.getSelectionStart(), textView.getSelectionEnd())
                            .toString()
                            .replace("<br>", " ")
                            .replace("\n", " ");

                    //根据item的ID处理点击事件
                    switch (menuItem.getItemId()) {
                        case R.id.GoogleTranslate:
                            actionMode.finish();//收起操作菜单

                            Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            intent.setPackage("com.google.android.apps.translate");
                            intent.setType("text/plain");

                            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, selection);
                            //intent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, selection);

                            startActivity(intent);
                            break;
                    }
                    return false;//返回true则系统的"复制"、"搜索"之类的item将无效，只有自定义item有响应
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {

                }

                @Override
                public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
                    //可选  用于改变弹出菜单的位置
                    super.onGetContentRect(mode, view, outRect);
                }
            };
            textView.setCustomSelectionActionModeCallback(callback2);
        }
    }
}
