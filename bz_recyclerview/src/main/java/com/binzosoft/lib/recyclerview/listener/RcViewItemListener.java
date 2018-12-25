package com.binzosoft.lib.recyclerview.listener;

import android.view.View;

public interface RcViewItemListener {
    void onClick(int position, View view);

    void onLongClick(int position, View view);
}
