package com.binzosoft.lib.demos;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.binzosoft.lib.file_manager.FileRestriction;
import com.binzosoft.lib.util.PermissionUtil;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;

public class FileManagerDemo extends AppCompatActivity implements Button.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private Button button1, button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtil.requestPermissions(this);
        setContentView(R.layout.activity_file_manager);
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button1) {
            Intent intent = new Intent();
            intent.setClass(this, MyFileManagerActivity.class);
            FileRestriction restriction = new FileRestriction();
            String defaultRoot = getExternalStorageDirectory().getPath() + File.separator + "learn";
            Log.i(TAG, "defaultRoot:" + defaultRoot);
            restriction.setRoot(defaultRoot);
            restriction.includeExtensions(new String[]{
                    //".k.mp4"
                    ".mp4"
            });
            intent.putExtra(FileRestriction.PARCELABLE_NAME, restriction);
            startActivity(intent);
        } else if (id == R.id.button2) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
