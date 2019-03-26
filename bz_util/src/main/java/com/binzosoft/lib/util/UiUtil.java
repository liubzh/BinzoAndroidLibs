package com.binzosoft.lib.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import static android.content.Context.UI_MODE_SERVICE;

public class UiUtil {

    public static final String TAG = "UiUtil";

    public static boolean isTelevision(Context context) {
        boolean isTV = false;
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.d(TAG, "Running on a TV Device");
            isTV = true;
        } else {
            Log.d(TAG, "Running on a non-TV Device");
        }
        return isTV;
    }
}
