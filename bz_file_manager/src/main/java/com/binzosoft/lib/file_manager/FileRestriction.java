package com.binzosoft.lib.file_manager;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 文件操作中，经常会遇到一些需求，如只显示某一类扩展名文件，PNG、MP4等
 * 可使用这个类进行限制/约束
 */
public class FileRestriction implements Parcelable {

    private ArrayList<String> INCLUDE_EXTENSIONS = new ArrayList<>(2);
    private ArrayList<String> EXCLUDE_EXTENSIONS = new ArrayList<>(2);

    private ArrayList<String> INCLUDE_DIRECTORIES = new ArrayList<>(2);
    private ArrayList<String> EXCLUDE_DIRECTORIES = new ArrayList<>(2);

    public FileRestriction(Parcel parcel) {
        parcel.readStringList(INCLUDE_EXTENSIONS);
        parcel.readStringList(EXCLUDE_EXTENSIONS);

        parcel.readStringList(INCLUDE_DIRECTORIES);
        parcel.readStringList(EXCLUDE_DIRECTORIES);
    }

    public void includeExtensions(String[] extensions) {
        for (String extension : extensions) {
            INCLUDE_EXTENSIONS.add(extension);
        }
    }

    public void excludeExtensions(String[] extensions) {
        for (String extension : extensions) {
            EXCLUDE_EXTENSIONS.add(extension);
        }
    }

    public void includeDirectories(String[] directories) {
        for (String extension : directories) {
            INCLUDE_EXTENSIONS.add(extension);
        }
    }

    public void excludeDirectories(String[] directories) {
        for (String extension : directories) {
            EXCLUDE_EXTENSIONS.add(extension);
        }
    }

    public static final Parcelable.Creator<FileRestriction> CREATOR
            = new Parcelable.Creator<FileRestriction>() {
        public FileRestriction createFromParcel(Parcel in) {
            return new FileRestriction(in);
        }

        public FileRestriction[] newArray(int size) {
            return new FileRestriction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(INCLUDE_EXTENSIONS);
        dest.writeStringList(EXCLUDE_EXTENSIONS);

        dest.writeStringList(INCLUDE_DIRECTORIES);
        dest.writeStringList(EXCLUDE_DIRECTORIES);
    }
}
