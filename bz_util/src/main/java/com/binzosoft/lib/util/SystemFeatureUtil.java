package com.binzosoft.lib.util;

import android.content.Context;
import android.util.Log;

public class SystemFeatureUtil {

    private static final String TAG = "SystemFeatureUtil";

    public static boolean hasTelephonyHardware(Context context) {
        // Check if the telephony hardware feature is available.
        if (context.getPackageManager().hasSystemFeature("android.hardware.telephony")) {
            Log.d("HardwareFeatureTest", "Device can make phone calls");
            return true;
        }
        return false;
    }

    public static boolean hasTouchScreen(Context context) {
        // Check if android.hardware.touchscreen feature is available.
        if (context.getPackageManager().hasSystemFeature("android.hardware.touchscreen")) {
            Log.d("HardwareFeatureTest", "Device has a touch screen.");
            return true;
        }
        return false;
    }
}
