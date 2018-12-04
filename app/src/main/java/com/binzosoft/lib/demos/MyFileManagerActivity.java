package com.binzosoft.lib.demos;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.binzosoft.lib.file_manager.FileManagerActivity;
import com.binzosoft.lib.video.VideoActivity;

import java.io.File;

public class MyFileManagerActivity extends FileManagerActivity {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onSelected(String path) {
        Log.i(TAG, "onSelected:" + path);
        Intent intent = new Intent();
        intent.setClass(this, VideoActivity.class);
        intent.setData(Uri.fromFile(new File(path)));
        startActivity(intent);
    }

}
