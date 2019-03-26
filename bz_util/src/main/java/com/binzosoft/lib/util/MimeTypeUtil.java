package com.binzosoft.lib.util;

import android.webkit.MimeTypeMap;

public class MimeTypeUtil {

    private static final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

    public static boolean isVideoFile(String path) {
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(
                path.substring(path.lastIndexOf(".") + 1));
        return mimeType.startsWith("video/");
    }

    public static boolean isAudioFile(String path) {
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(
                path.substring(path.lastIndexOf(".") + 1));
        return mimeType.startsWith("audio/");
    }

    public static boolean isImageFile(String path) {
        String mimeType = mimeTypeMap.getMimeTypeFromExtension(
                path.substring(path.lastIndexOf(".") + 1));
        return mimeType.startsWith("image/");
    }

}
