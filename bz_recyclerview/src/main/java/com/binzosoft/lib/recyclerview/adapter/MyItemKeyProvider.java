package com.binzosoft.lib.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import com.binzosoft.lib.recyclerview.model.Item;

import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;

public class MyItemKeyProvider extends ItemKeyProvider {
    private final List<Item> itemList;

    public MyItemKeyProvider(int scope, List<Item> itemList) {
        super(scope);
        this.itemList = itemList;
    }

    @Nullable
    @Override
    public Object getKey(int position) {
        return itemList.get(position);
    }

    @Override
    public int getPosition(@NonNull Object key) {
        return itemList.indexOf(key);
    }
}
