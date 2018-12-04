package com.binzosoft.lib.file_manager.listener;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class RcViewOnClickListener implements RecyclerView.OnChildAttachStateChangeListener {

    private final String TAG = "RcViewOnClickListener";

    private RecyclerView mRecyclerView;
    private RcViewItemListener itemListener;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v != null && itemListener != null) {
                int position = mRecyclerView.getChildAdapterPosition(v);
                Log.i(TAG, "onClick() position:" + position);
                Toast.makeText(mRecyclerView.getContext(), "onClick() position:" + position, Toast.LENGTH_SHORT).show();
                itemListener.onClick(position, v);
            }
        }
    };

    View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v != null && itemListener != null) {
                int position = mRecyclerView.getChildAdapterPosition(v);
                Log.i(TAG, "onLongClick() position:" + position);
                itemListener.onLongClick(position, v);
                return true;
            }
            return false;
        }
    };

    public RcViewOnClickListener(RecyclerView recyclerView, final RcViewItemListener itemListener) {
        mRecyclerView = recyclerView;
        this.itemListener = itemListener;
    }

    @Override
    public void onChildViewAttachedToWindow(View view) {
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {

    }

}