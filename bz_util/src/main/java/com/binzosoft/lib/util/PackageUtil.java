package com.binzosoft.lib.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class PackageUtil {

    private static final String TAG = "PackageUtil";

    /**
     * 判断应用是否安装
     * 示例：appInstalled(mContext, "com.example.app")
     * @param context
     * @param packageName
     * @return
     */
    public static boolean appInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName is empty.");
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        if (packageInfoList != null) {
            for (int i = 0; i < packageInfoList.size(); i++) {
                PackageInfo info = packageInfoList.get(i);

                String pn = info.packageName;
                if (packageName.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断某应用中是否存在某个Activity
     * 示例：activityExists(mContext, "com.example.app", "com.example.app.MainActivity")
     * 或者：activityExists(mContext, "com.example.app", ".MainActivity")
     * @param context
     * @param packageName
     * @param activityName
     * @return
     */
    public static boolean activityExists(Context context, String packageName, String activityName) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName is empty.");
        } else if (TextUtils.isEmpty(activityName)) {
            throw new IllegalArgumentException("activityName is empty.");
        }
        final PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                ActivityInfo[] infos = packageInfo.activities;
                if (infos == null) {
                    return false;
                }
                for (ActivityInfo info : infos) {
                    Log.i(TAG, String.format("activity name: %s", info.name));
                    if (info.name.endsWith(activityName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断某应用中是否存在某个Service
     * 示例：serviceExists(mContext, "com.example.app", "com.example.app.MyService")
     * 或者：serviceExists(mContext, "com.example.app", ".MyService")
     * @param context
     * @param packageName
     * @param serviceName
     * @return
     */
    public static boolean serviceExists(Context context, String packageName, String serviceName) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName is empty.");
        } else if (TextUtils.isEmpty(serviceName)) {
            throw new IllegalArgumentException("serviceName is empty.");
        }
        final PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES);
            if (packageInfo != null) {
                ServiceInfo[] infos = packageInfo.services;
                if (infos == null) {
                    return false;
                }
                for (ServiceInfo info : infos) {
                    Log.i(TAG, String.format("service name: %s", info.name));
                    if (info.name.endsWith(serviceName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
