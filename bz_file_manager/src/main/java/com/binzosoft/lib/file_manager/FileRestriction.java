package com.binzosoft.lib.file_manager;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 文件操作中，经常会遇到一些需求，如只显示某一类扩展名文件，PNG、MP4等
 * 可使用这个类进行限制/约束，INCLUDE/EXCLUDE 灵感来源于 grep 命令
 */
public class FileRestriction implements Parcelable {

    private final String TAG = "FileRestriction";

    public static final String PARCELABLE_NAME = "FileRestriction";

    private ArrayList<String> INCLUDE_EXTENSIONS = new ArrayList<>();
    private ArrayList<String> EXCLUDE_EXTENSIONS = new ArrayList<>();

    private ArrayList<String> INCLUDE_DIRECTORIES = new ArrayList<>();
    private ArrayList<String> EXCLUDE_DIRECTORIES = new ArrayList<>();

    private String ROOT;

    public FileRestriction() {

    }

    public FileRestriction(Parcel parcel) {
        ROOT = parcel.readString();

        parcel.readStringList(INCLUDE_EXTENSIONS);
        parcel.readStringList(EXCLUDE_EXTENSIONS);

        parcel.readStringList(INCLUDE_DIRECTORIES);
        parcel.readStringList(EXCLUDE_DIRECTORIES);
    }

    public void setRoot(String root) {
        this.ROOT = root;
    }

    public String getRoot() {
        return this.ROOT;
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
        dest.writeString(ROOT);

        dest.writeStringList(INCLUDE_EXTENSIONS);
        dest.writeStringList(EXCLUDE_EXTENSIONS);

        dest.writeStringList(INCLUDE_DIRECTORIES);
        dest.writeStringList(EXCLUDE_DIRECTORIES);
    }

    public File[] filter(String dir) {
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory()) {
            return null;
        }
        String[] paths = file.list();
        ArrayList<File> arrayList = new ArrayList<>();
        for (String name : paths) {
            String pth = dir + File.separator + name;
            Log.i(TAG, "pth:" + pth);
            File f = new File(pth);
            boolean add = true;
            if (f.isDirectory()) {
                Log.i(TAG, "is directory");
                // 目录
                if (INCLUDE_DIRECTORIES.size() > 0 && !INCLUDE_DIRECTORIES.contains(pth)) {
                    Log.i(TAG, "not in INCLUDE_DIRECTORIES");
                    add = false;
                }
                if (EXCLUDE_DIRECTORIES.size() > 0 && EXCLUDE_DIRECTORIES.contains(pth)) {
                    Log.i(TAG, "in EXCLUDE_DIRECTORIES");
                    add = false;
                }
            } else {
                Log.i(TAG, "is file");
                // 文件
                if (INCLUDE_EXTENSIONS.size() > 0) {
                    boolean contain = false;
                    for (String ext : INCLUDE_EXTENSIONS) {
                        if (pth.endsWith(ext)) {
                            contain = true;
                        }
                    }
                    if (!contain) {
                        add = false;
                    }
                }
                if (EXCLUDE_EXTENSIONS.size() > 0) {
                    boolean contain = false;
                    for (String ext : EXCLUDE_EXTENSIONS) {
                        if (pth.endsWith(ext)) {
                            contain = true;
                        }
                    }
                    if (contain) {
                        add = false;
                    }
                }
            }
            if (add) {
                Log.i(TAG, "add");
                arrayList.add(new File(pth));
            }
        }

        Collections.sort(arrayList, new SortByName());
        Log.i(TAG, "size:" + arrayList.size());
        File[] filtered = new File[arrayList.size()];
        arrayList.toArray(filtered);
        return filtered;
    }

    class SortByName implements Comparator {
        public int compare(Object o1, Object o2) {
            File f1 = (File) o1;
            File f2 = (File) o2;
            return f1.getName().compareTo(f2.getName());
        }
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("ROOT:").append(ROOT).append("; ");

        if (INCLUDE_EXTENSIONS.size() > 0) {
            stringBuffer.append("INCLUDE_EXT:[");
            for (String string : INCLUDE_EXTENSIONS) {
                stringBuffer.append(string).append(",");
            }
            stringBuffer.append("]; ");
        }

        if (EXCLUDE_EXTENSIONS.size() > 0) {
            stringBuffer.append("EXCLUDE_EXT:[");
            for (String string : EXCLUDE_EXTENSIONS) {
                stringBuffer.append(string).append(",");
            }
            stringBuffer.append("]; ");
        }

        if (INCLUDE_DIRECTORIES.size() > 0) {
            stringBuffer.append("INCLUDE_DIR:[");
            for (String string : INCLUDE_DIRECTORIES) {
                stringBuffer.append(string).append(",");
            }
            stringBuffer.append("]; ");
        }

        if (EXCLUDE_DIRECTORIES.size() > 0) {
            stringBuffer.append("EXCLUDE_DIR:[");
            for (String string : EXCLUDE_DIRECTORIES) {
                stringBuffer.append(string).append(",");
            }
            stringBuffer.append("]; ");
        }
        return super.toString();
    }
}
