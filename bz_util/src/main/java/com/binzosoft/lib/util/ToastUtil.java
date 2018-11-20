package com.binzosoft.lib.util;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Binzo on 2018/11/20.
 * Toast 统一管理类
 */

public class ToastUtil {

    /**
     * Toast显示开关，默认开启，false即关闭Toast显示
     */
    private static boolean SHOW_TOAST = true;

    private static Toast mToast = null;//全局唯一Toast实例

    /* private 表示此类不能被实例化*/
    private ToastUtil() {
        throw new UnsupportedOperationException("ToastUtil can not be instantiated.");
    }

    /**
     * 全局控制是否显示Toast
     *
     * @param isShowToast
     */
    public static void setShowToast(boolean isShowToast) {
        SHOW_TOAST = isShowToast;
    }

    public static boolean isShowToast() {
        return SHOW_TOAST;
    }

    /**
     * 取消Toast显示，见 Toast.cancel()
     */
    public void cancel() {
        if (SHOW_TOAST && mToast != null) {
            mToast.cancel();
        }
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, CharSequence message) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param resId   资源ID:getResources().getString(R.string.xxxxxx);
     */
    public static void showShort(Context context, int resId) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, CharSequence message) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param resId   资源ID:getResources().getString(R.string.xxxxxx);
     */
    public static void showLong(Context context, int resId) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        mToast.show();
    }

    /**
     * 自定义带图片和文字的Toast，最终的效果就是上面是图片，下面是文字
     *
     * @param context
     * @param message
     * @param iconResId 图片的资源id,如:R.drawable.icon
     * @param duration
     */
    public static void showWithIcon(Context context, CharSequence message, int iconResId, int duration) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, duration);
        LinearLayout toastView = (LinearLayout) mToast.getView();
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(iconResId);
        toastView.addView(imageView, 0);
        mToast.show();
    }

    /**
     * 自定义Toast的View
     *
     * @param context
     * @param duration Toast.LENGTH_LONG or Toast.LENGTH_SHORT
     * @param view     显示自己的View
     */
    public static void showCustom(Context context, View view, int duration) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, "", duration);
        mToast.setView(view);
        mToast.show();
    }

    /**
     * 自定义Toast的位置
     *
     * @param context
     * @param message
     * @param duration 单位:毫秒
     * @param gravity
     * @param xOffset
     * @param yOffset
     */
    public static void showCustom(Context context, CharSequence message, int duration, int gravity, int xOffset, int yOffset) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, duration);
        mToast.setGravity(gravity, xOffset, yOffset);
        mToast.show();
    }

    /**
     * 自定义Toast,针对类型CharSequence
     *
     * @param context
     * @param duration
     * @param view
     * @param gravity
     * @param xOffset
     * @param yOffset
     * @param horizontalMargin
     * @param verticalMargin
     */
    public static void showCustom(Context context, View view, int duration, int gravity, int xOffset, int yOffset, float horizontalMargin, float verticalMargin) {
        if (!SHOW_TOAST) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, "", duration);
        mToast.setView(view);
        mToast.setGravity(gravity, xOffset, yOffset);
        mToast.setMargin(horizontalMargin, verticalMargin);
        mToast.show();
    }

}
