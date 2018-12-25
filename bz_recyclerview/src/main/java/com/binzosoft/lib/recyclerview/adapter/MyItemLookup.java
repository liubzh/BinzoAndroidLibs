package com.binzosoft.lib.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;

/**
 * Created by brijesh on 27/3/18.
 */

public class MyItemLookup extends ItemDetailsLookup {

    private final RecyclerView recyclerView;

    public MyItemLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetailsLookup.ItemDetails getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof ItemListAdapter.ItemListViewHolder) {
                return ((ItemListAdapter.ItemListViewHolder) viewHolder).getItemDetails();
            }
        }

        return null;
    }
}
