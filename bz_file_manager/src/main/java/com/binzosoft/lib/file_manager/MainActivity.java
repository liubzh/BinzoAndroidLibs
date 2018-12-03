package com.binzosoft.lib.file_manager;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.binzosoft.lib.util.PermissionUtil;
import com.binzosoft.lib.util.ToastUtil;

import java.io.File;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private File[] fileList;
    private FileAdapter mAdapter;
    private Stack<String> pathStack = new Stack<>();
    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        PermissionUtil.requestPermissions(this);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentPath = Environment.getExternalStorageDirectory().getPath();
        Log.i(TAG, "currentPath:" + currentPath);
        fileList = new File(currentPath).listFiles();
        mAdapter = new FileAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemTouchListener(
                mRecyclerView, itemClickListener));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    protected void pathForward(String path) {
        if (currentPath != null && currentPath.equals(path)) {
            return;
        }
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }
        Log.i(TAG, "pathForward() path:" + path);
        pathStack.push(currentPath);
        Log.i(TAG, pathStack.toString());
        fileList = file.listFiles();
        mRecyclerView.setAdapter(new FileAdapter());
        currentPath = path;
    }

    /**
     * @return 是否返回上级目录成功
     */
    protected boolean pathBackward() {
        if (pathStack.isEmpty()) {
            return false;
        }
        String path = pathStack.pop();
        Log.i(TAG, "pathBackward() path:" + path);
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        fileList = file.listFiles();
        mRecyclerView.setAdapter(new FileAdapter());
        currentPath = path;
        return true;
    }

    private RecyclerViewItemTouchListener.OnItemClickListener itemClickListener =
            new RecyclerViewItemTouchListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view) {
                    TextView tv = view.findViewById(R.id.file_name);
                    if (tv == null) {
                        return;
                    }
                    String baseName = tv.getText().toString();
                    if (TextUtils.isEmpty(baseName)) {
                        return;
                    }
                    String path = currentPath + File.separator + baseName;
                    File file = new File(path);
                    Log.i(TAG, "path: " + path);
                    if (file.exists() && file.isDirectory()) {
                        pathForward(file.getPath());
                    }
                    Log.i(TAG, tv.getText().toString());
                    ToastUtil.showShort(MainActivity.this, "click: " + tv.getText());
                }

                @Override
                public void onItemLongClick(View view) {
                    /*
                    TextView tv = view.findViewById(R.id.file_name);
                    Log.i(TAG, tv.getText().toString());
                    ToastUtil.showShort(MainActivity.this, "long click: " + tv.getText());
                    */
                }
            };

    class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    MainActivity.this).inflate(R.layout.file_item, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.tv.setText(fileList[position].getName());
        }

        @Override
        public int getItemCount() {
            if (fileList == null) {
                return 0;
            }
            return fileList.length;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv;

            public MyViewHolder(View view) {
                super(view);
                tv = view.findViewById(R.id.file_name);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (pathBackward()) {
                return false;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
