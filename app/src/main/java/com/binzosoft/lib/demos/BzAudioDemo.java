package com.binzosoft.lib.demos;


import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

public class BzAudioDemo extends AppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
