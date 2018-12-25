package com.binzosoft.lib.recyclerview;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.binzosoft.lib.recyclerview.adapter.ItemListAdapter;
import com.binzosoft.lib.recyclerview.adapter.MyItemKeyProvider;
import com.binzosoft.lib.recyclerview.adapter.MyItemLookup;
import com.binzosoft.lib.recyclerview.listener.RcViewItemListener;
import com.binzosoft.lib.recyclerview.listener.RcViewOnClickListener;
import com.binzosoft.lib.recyclerview.model.Item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.OnDragInitiatedListener;
import androidx.recyclerview.selection.OnItemActivatedListener;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    MenuItem selectedItemCount;
    SelectionTracker selectionTracker;
    private RecyclerView itemListView;
    private ItemListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Item> itemList;
    private android.support.v7.view.ActionMode actionMode;

    private RcViewItemListener itemClickListener =
            new RcViewItemListener() {
                @Override
                public void onClick(int position, View view) {
                    Log.i(TAG, "onClick:" + position);
                }

                @Override
                public void onLongClick(int position, View view) {
                    Log.i(TAG, "onLongClick:" + position);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rv_activity_main);


        itemList = getRandomList();
        itemListView = findViewById(R.id.itemList);

        itemListView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        itemListView.setLayoutManager(mLayoutManager);
        itemListView.setItemAnimator(new DefaultItemAnimator());
        itemListView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mAdapter = new ItemListAdapter(itemList);
        itemListView.setAdapter(mAdapter);


        selectionTracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                itemListView,
                new MyItemKeyProvider(1, itemList),
                new MyItemLookup(itemListView),
                StorageStrategy.createLongStorage()
        )
                .withOnItemActivatedListener(new OnItemActivatedListener<Long>() {
                    @Override
                    public boolean onItemActivated(@NonNull ItemDetailsLookup.ItemDetails<Long> item, @NonNull MotionEvent e) {
                        //Log.d(TAG, "Selected ItemId: " + item.toString());
                        Log.d(TAG, "Selected ItemId: " + item.getPosition());
                        return true;
                    }
                })
                .withOnDragInitiatedListener(new OnDragInitiatedListener() {
                    @Override
                    public boolean onDragInitiated(@NonNull MotionEvent e) {
                        Log.d(TAG, "onDragInitiated");
                        return true;
                    }

                })
                .build();
        mAdapter.setSelectionTracker(selectionTracker);
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
            @Override
            public void onItemStateChanged(@NonNull Object key, boolean selected) {
                super.onItemStateChanged(key, selected);
            }

            @Override
            public void onSelectionRefresh() {
                super.onSelectionRefresh();
            }

            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (selectionTracker.hasSelection() && actionMode == null) {
                    actionMode = startSupportActionMode(new ActionModeController(MainActivity.this, selectionTracker));
                    setMenuItemTitle(selectionTracker.getSelection().size());
                } else if (!selectionTracker.hasSelection() && actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                } else {
                    setMenuItemTitle(selectionTracker.getSelection().size());
                }
                Iterator<Item> itemIterable = selectionTracker.getSelection().iterator();
                while (itemIterable.hasNext()) {
                    Log.i(TAG, itemIterable.next().getItemName());
                }
            }

            @Override
            public void onSelectionRestored() {
                super.onSelectionRestored();
            }
        });

        if (savedInstanceState != null) {
            selectionTracker.onRestoreInstanceState(savedInstanceState);
        }

//        itemListView.addOnChildAttachStateChangeListener(
//                new RcViewOnClickListener(itemListView, itemClickListener)
//        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        selectionTracker.onSaveInstanceState(outState);
    }

    public void setMenuItemTitle(int selectedItemSize) {
        selectedItemCount.setTitle("" + selectedItemSize);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rv_main_menu, menu);
        selectedItemCount = menu.findItem(R.id.action_item_count);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_delete) {

        } else if (i == R.id.action_clear) {
            selectionTracker.clearSelection();

        }
        return true;
    }

    private List<Item> getRandomList() {
        Random random = new Random();
        List<Item> items = new ArrayList<>();
        Item item;
        for (int i = 1; i <= 100; i++) {
            item = new Item();
            item.setItemId(i);
            item.setItemName("Item Name " + i);
            item.setItemPrice(random.nextFloat() * 1000);
            items.add(item);
        }

        return items;
    }
}
