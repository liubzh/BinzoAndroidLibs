package com.binzosoft.lib.file_manager;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.binzosoft.lib.file_manager.listener.OnFileSelected;
import com.binzosoft.lib.file_manager.listener.RcViewItemListener;
import com.binzosoft.lib.file_manager.listener.RcViewOnClickListener;
import com.binzosoft.lib.util.PermissionUtil;
import com.binzosoft.lib.util.ToastUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.Stack;

public class FileManagerActivity extends AppCompatActivity implements OnFileSelected, FileFilter {

    private final String TAG = "FileManagerActivity";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private File[] fileList;
    private FileListAdapter mAdapter;
    private Stack<PathStackItem> pathStack = new Stack<>();
    private String currentPath;
    protected String rootDir = Environment.getExternalStorageDirectory().getPath();

    @Override
    public boolean accept(File pathname) {
        return true; // 默认不做任何约束，可在子类中重写此方法进行文件列表约束
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        PermissionUtil.requestPermissions(this);
        setContentView(R.layout.fm_activity_file_manager);

        mRecyclerView = findViewById(R.id.fmRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setItemDecoration(mRecyclerView);

        currentPath = rootDir;
        fileList = new File(currentPath).listFiles(this);
        mAdapter = new FileListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // 以下是两种设置条目点击事件的方式，二选一
        // .1. 可以同时兼顾 Touch 和 Key 两种方式
        mRecyclerView.addOnChildAttachStateChangeListener(
                new RcViewOnClickListener(mRecyclerView, itemClickListener)
        );
        // .2. 只能处理 Touch 方式
//        mRecyclerView.addOnItemTouchListener(
//                new RcViewOnTouchListener(mRecyclerView, itemClickListener)
//        );

        // 根据API不同，设置ScrollListener
        if (!setOnScrollChangeListener()) {
            setOnScrollListener();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setOnScrollListener() {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        };
        mRecyclerView.addOnScrollListener(onScrollListener);
    }

    private boolean setOnScrollChangeListener() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            View.OnScrollChangeListener onScrollChangeListener = new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                }
            };
            mRecyclerView.setOnScrollChangeListener(onScrollChangeListener);
            return true;
        }
        return false;
    }

    protected void pathForward(final String path) {
        if (currentPath != null && currentPath.equals(path)) {
            return;
        }
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }
        Log.i(TAG, "pathForward() path:" + path);

        PathStackItem pathStackItem = new PathStackItem();
        pathStackItem.setPath(currentPath);
        View topView = mLayoutManager.getChildAt(0);  //获取可视的第一个view
        int lastOffset = topView.getTop();  //获取与该view的顶部的偏移量
        int lastPosition = mLayoutManager.getPosition(topView);
        pathStackItem.setLastOffset(lastOffset);
        pathStackItem.setLastPosition(lastPosition);

        pathStack.push(pathStackItem);
        Log.i(TAG, pathStack.toString());
        fileList = file.listFiles(this);
//        if (restriction != null) {
//            fileList = restriction.filter(path);
//        }
        mAdapter = new FileListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        currentPath = path;
    }

    protected void pathBackward() {
        if (pathStack.isEmpty()) {
            return;
        }
        PathStackItem pathStackItem = pathStack.pop();
        String path = pathStackItem.getPath();
        //Log.i(TAG, "pathBackward() path:" + path);
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        fileList = file.listFiles(this);
        mAdapter = new FileListAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // 确定条目并滚动到相应位置
        final String focusDirName = new File(currentPath).getName();
        final PathStackItem pathItem = pathStackItem;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mRecyclerView.removeOnScrollListener(this);
                int position = -1;
                Log.i(TAG, String.format("currentDirName:%s", focusDirName));
                for (int i = 0; i < fileList.length; i++) {
                    if (focusDirName.equals(fileList[i].getName())) {
                        position = i;
                        break;
                    }
                }
                Log.i(TAG, String.format("lastPosition:%d, lastOffset:%d, position:%d",
                        pathItem.getLastPosition(), pathItem.getLastOffset(), position));
                View view = null;
                if (position < 0) {
                    int firstVisibleItemPosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (firstVisibleItemPosition > 0) {
                        view = mLayoutManager.findViewByPosition(firstVisibleItemPosition);
                    }
                } else {
                    view = mLayoutManager.findViewByPosition(position);
                }
                if (view != null) {
                    view.requestFocus();
                } else {
                    Log.i(TAG, "not found view to request focus.");
                }
            }
        });
        mLayoutManager.scrollToPositionWithOffset(pathStackItem.getLastPosition(),
                pathStackItem.getLastOffset());
        currentPath = path;
    }

    private RcViewItemListener itemClickListener =
            new RcViewItemListener() {
                @Override
                public void onClick(int position, View view) {
                    TextView tv = view.findViewById(R.id.fm_file_name);
                    if (tv == null) {
                        return;
                    }
                    String baseName = tv.getText().toString();
                    if (TextUtils.isEmpty(baseName)) {
                        return;
                    }
                    String path = currentPath + File.separator + baseName;
                    File file = new File(path);
                    //Log.i(TAG, "path: " + path);
                    if (file.exists() && file.isDirectory()) {
                        pathForward(file.getPath());
                    } else {
                        onSelected(path);
                    }

                }

                @Override
                public void onLongClick(int position, View view) {
                    TextView tv = view.findViewById(R.id.fm_file_name);
                    ToastUtil.showShort(FileManagerActivity.this,
                            String.format("onLongClick %s, position %d", tv.getText(), position));
                }
            };

    private void setItemDecoration(RecyclerView recyclerView) {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
    }

    class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    FileManagerActivity.this).inflate(R.layout.fm_file_item, parent,
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
                tv = view.findViewById(R.id.fm_file_name);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (pathStack != null && !pathStack.isEmpty()) {
                pathBackward();
                return false;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onSelected(String path) {
        // 当选定一个文件，就会调用这个方法。
        // 可继承这个Activity，然后重写这个方法
    }

    /*
    class USBDiskManager extends BroadcastReceiver {
        private static final String ACTION_USB_PERMISSION =
                "com.binzosoft.lib.file_manager.USB_PERMISSION";
        private Context context;
        private PendingIntent permissionIntent;

        public USBDiskManager(Context context) {
            this.context = context;
            this.permissionIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(ACTION_USB_PERMISSION), 0);
        }

        private ArrayList<File> DISKS = new ArrayList<File>() {{
            add(Environment.getExternalStorageDirectory());
        }};

        public ArrayList<File> getDisks() {
            return DISKS;
        }

        public int getDiskCount() {
            return DISKS.size();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, String.format("USBDiskManager received action:[%s]", action));

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    //your code
                    if (device.getDeviceClass() == UsbConstants.USB_CLASS_MASS_STORAGE && !usbManager.hasPermission(device)) {
                        usbManager.requestPermission(device, permissionIntent);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // 插入USB设备
                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {
                    Log.i(TAG, String.format("deviceClass:%d, deviceName:%s",
                            usbDevice.getDeviceClass(), usbDevice.getDeviceName()));
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            Log.i(TAG, "permission granted for device " + device);
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }

        public void register() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_USB_PERMISSION);
            context.registerReceiver(this, filter);
        }

        public void unregister() {
            try {
                context.unregisterReceiver(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    */
}
