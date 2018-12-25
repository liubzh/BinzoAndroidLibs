package com.binzosoft.lib.recyclerview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.binzosoft.lib.recyclerview.listener.RcViewItemListener;
import com.binzosoft.lib.recyclerview.listener.RcViewOnClickListener;
import com.binzosoft.lib.recyclerview.model.Item;
import com.binzosoft.lib.util.PermissionUtil;
import com.binzosoft.lib.util.ToastUtil;

import java.util.ArrayList;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.OnItemActivatedListener;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;

public class DemoActivity extends AppCompatActivity {

    private final String TAG = "FileManagerActivity";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Item> itemList = new ArrayList<Item>() {{

    }};
    private MyListAdapter mAdapter;
    private SelectionTracker mSelectionTracker;

    private SelectionTracker.SelectionObserver<Long> selectionObserver =
            new SelectionTracker.SelectionObserver<Long>() {
                @Override
                public void onItemStateChanged(@NonNull Long key, boolean selected) {
                    super.onItemStateChanged(key, selected);
                    Log.i(TAG, String.format("onItemStateChanged(%s, %s)", key, selected));
                }

                @Override
                public void onSelectionRefresh() {
                    super.onSelectionRefresh();
                    Log.i(TAG, String.format("onSelectionRefresh()"));
                }

                @Override
                public void onSelectionChanged() {
                    super.onSelectionChanged();
                    Log.i(TAG, String.format("onSelectionChanged()"));
                }

                @Override
                public void onSelectionRestored() {
                    super.onSelectionRestored();
                    Log.i(TAG, String.format("onSelectionRestored()"));
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        PermissionUtil.requestPermissions(this);
        setContentView(R.layout.rv_activity_demo);

        mRecyclerView = findViewById(R.id.rvRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setItemDecoration(mRecyclerView);

        mAdapter = new MyListAdapter();
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
//        if (!setOnScrollChangeListener()) {
//            setOnScrollListener();
//        }

        // 实现选择条目的功能
        mSelectionTracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                mRecyclerView,
                new StableIdKeyProvider(mRecyclerView),
                new MyDetailsLookup(mRecyclerView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(myItemActivatedListener)
                .build();

        mSelectionTracker.addObserver(selectionObserver);

    }

    final class MyDetailsLookup extends ItemDetailsLookup {

        private final RecyclerView mRecyclerView;

        MyDetailsLookup(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        public ItemDetails getItemDetails(MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                if (holder instanceof MyListAdapter.MyViewHolder) {
                    return ((MyListAdapter.MyViewHolder) holder).getItemDetails();
                }
            }
            return null;
        }
    }

    private OnItemActivatedListener myItemActivatedListener = new OnItemActivatedListener() {
        @Override
        public boolean onItemActivated(@NonNull ItemDetailsLookup.ItemDetails itemDetails, @NonNull MotionEvent motionEvent) {
            Log.i(TAG, "onItemActivated");
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void setOnScrollListener() {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        };
        mRecyclerView.setOnScrollListener(onScrollListener);
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

    private RcViewItemListener itemClickListener =
            new RcViewItemListener() {
                @Override
                public void onClick(int position, View view) {
                    TextView tv = view.findViewById(R.id.rvItemName);
                    if (tv == null) {
                        return;
                    }
//                    boolean activated = view.isActivated();
//                    view.setActivated(!activated);
                    Long pos = Long.valueOf(position);
                    if (mSelectionTracker.isSelected(pos)) {
                        mSelectionTracker.deselect(pos);
                    } else {
                        mSelectionTracker.select(pos);
                    }
                    //mAdapter.notifyItemChanged(position);
                    ToastUtil.showShort(DemoActivity.this,
                            String.format("onClick %s, position %d", tv.getText(), position));
                }

                @Override
                public void onLongClick(int position, View view) {
                    TextView tv = view.findViewById(R.id.rvItemName);
                    if (tv == null) {
                        return;
                    }
                    ToastUtil.showShort(DemoActivity.this,
                            String.format("onLongClick %s, position %d", tv.getText(), position));
                }
            };

    private void setItemDecoration(RecyclerView recyclerView) {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
    }

    class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    DemoActivity.this).inflate(R.layout.rv_list_item, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Log.i(TAG, "onBindViewHolder() position:" + position);

            Item item = itemList.get(position);
            holder.bind(item, mSelectionTracker.isSelected(item));
        }

        @Override
        public int getItemCount() {
            if (itemList == null) {
                return 0;
            }
            return itemList.size();
        }

        public class MyItemDetails extends ItemDetailsLookup.ItemDetails {
            private final int adapterPosition;
            private final Item selectionKey;

            public MyItemDetails(int adapterPosition, Item selectionKey) {
                this.adapterPosition = adapterPosition;
                this.selectionKey = selectionKey;
            }

            @Override
            public int getPosition() {
                return adapterPosition;
            }

            @Nullable
            @Override
            public Object getSelectionKey() {
                return selectionKey;
            }
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView itemName;

            public MyViewHolder(View view) {
                super(view);
                itemName = view.findViewById(R.id.rvItemName);
            }

            public final void bind(Item item, boolean isActive) {
                itemView.setActivated(isActive);
                //itemPrice.setText(item.getItemPrice() + "$");
                itemName.setText(item.getItemName());
                //itemId.setText(item.getItemId() + "");
            }

            public ItemDetailsLookup.ItemDetails getItemDetails() {
                return new MyItemDetails(getAdapterPosition(), itemList.get(getAdapterPosition()));
            }
        }
    }
}
