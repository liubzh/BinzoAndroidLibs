package com.binzosoft.lib.demos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.binzosoft.lib.file_manager.FileManagerActivity;
import com.binzosoft.lib.util.MimeTypeUtil;
import com.binzosoft.lib.video.ExoPlayerAudioActivity;
import com.binzosoft.lib.video.ExoPlayerVideoActivity;
import com.binzosoft.lib.video.VideoViewActivity;

import java.io.File;

public class MyFileManagerActivity extends FileManagerActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName();
        if (name.startsWith(".")) {
            return false; //不显示隐藏文件
        }
        if (pathname.isDirectory()) {
            return true; //显示目录
        } else if (name.endsWith(".mp4")) {
            return true; //显示 .mp4 视频文件
        } else if (name.endsWith(".mp3") || name.endsWith(".wav")) {
            return true; //显示 .mp3 .wav 音频文件
        } else if (name.endsWith(".txt")) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //rootDir = Environment.getExternalStorageDirectory().getPath() + File.separator + "learn";
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSelected(String path) {
        Log.i(TAG, "onSelected:" + path);
        //startVideoViewActivity(path);
        if (MimeTypeUtil.isVideoFile(path)) {
            startExoPlayerVideoActivity(path);
        } else if (MimeTypeUtil.isAudioFile(path)) {
            startExoPlayerAudioActivity(path);
        }
    }

    private void startVideoViewActivity(String path) {
        Intent intent = new Intent();
        intent.setClass(this, VideoViewActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

    private void startExoPlayerVideoActivity(String path) {
//        Metadata metadata = Metadata.retrieve(path);
//        Log.i(TAG, "metadata:" + metadata);

        Intent intent = new Intent();
        intent.setClass(this, ExoPlayerVideoActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

    private void startExoPlayerAudioActivity(String path) {
//        Metadata metadata = Metadata.retrieve(path);
//        Log.i(TAG, "metadata:" + metadata);

        Intent intent = new Intent();
        intent.setClass(this, ExoPlayerAudioActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

}
