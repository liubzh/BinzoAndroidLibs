package com.binzosoft.lib.demos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.binzosoft.lib.file_manager.FileManagerActivity;
import com.binzosoft.lib.util.media.Metadata;
import com.binzosoft.lib.video.ExoPlayerActivity;
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
            return true; //显示mp4视频文件
        } else if (name.endsWith(".mp3")) {
            return true; //显示mp3音频文件
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
        startExoPlayerActivity(path);
    }

    private void startVideoViewActivity(String path) {
        Intent intent = new Intent();
        intent.setClass(this, VideoViewActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

    private void startExoPlayerActivity(String path) {
        Metadata metadata = Metadata.retrieve(path);
        Log.i(TAG, "metadata:" + metadata);

        Intent intent = new Intent();
        intent.setClass(this, ExoPlayerActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

}
