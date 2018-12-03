package com.binzosoft.lib.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * 动态申请权限工具类。
 * AndroidManifest中指定的权限：android.Manifest.permission.
 */
public class PermissionUtil {

    public static final String TAG = "PermissionUtil";

    public static final int REQUEST_CODE = 0x0010;

    /**
     * 一般应用，必须都需要联网和读写外部存储权限。所以列在必备权限里。
     * 如果需要其它权限，可使用 addNecessaryPermission() 进行添加
     */
    public static final ArrayList<String> PERMISSIONS = new ArrayList<String>() {{
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        add(Manifest.permission.INTERNET);
    }};

    /**
     * 添加动态申请的权限
     *
     * @param permission
     */
    public static void addPermission(String permission) {
        boolean contained = false;
        for (String perm : PERMISSIONS) {
            if (perm.equals(permission)) {
                // 如果已经存在，不添加
                contained = true;
                break;
            }
        }
        if (!contained) {
            PERMISSIONS.add(permission);
        }
    }

    /**
     * 判断应用权限是否都可正常获取，如果不能，提示用户打开相关权限。
     *
     * @param activity
     * @return 如果权限都请求正常，则返回 true，如果有被拒请求，返回 false
     */
    public static boolean requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[PERMISSIONS.size()];
            PERMISSIONS.toArray(permissions);
            boolean grantedAll = true;
            for (String permission : PERMISSIONS) {
                int result = ContextCompat.checkSelfPermission(activity, permission);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false;
                    break;
                }
            }
            if (!grantedAll) {
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * Activity中，请求权限的结果，可重写 onRequestPermissionsResult 方法进行确认
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            boolean grantedAll = true;
            for (int i = 0; i < permissions.length; i++) {
                Log.i(TAG, String.format("%s is %s.", permissions[i],
                        grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    grantedAll = false;
                }
            }
            if (!grantedAll) {
                String[] requestPermissions = new String[PERMISSIONS.size()];
                PERMISSIONS.toArray(requestPermissions);
                ActivityCompat.requestPermissions(activity, requestPermissions, REQUEST_CODE);
            }
        }
    }

}
