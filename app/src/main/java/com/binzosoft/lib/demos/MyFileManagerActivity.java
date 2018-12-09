package com.binzosoft.lib.demos;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.binzosoft.lib.file_manager.FileManagerActivity;
import com.binzosoft.lib.video.ExoPlayerActivity;
import com.binzosoft.lib.video.VideoViewActivity;

import java.io.File;

public class MyFileManagerActivity extends FileManagerActivity {

    private final String TAG = getClass().getSimpleName();

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
        Intent intent = new Intent();
        intent.setClass(this, ExoPlayerActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

}
