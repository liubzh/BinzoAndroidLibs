package com.binzosoft.lib.recyclerview.listener;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class RcViewOnTouchListener extends RecyclerView.SimpleOnItemTouchListener {

    private final String TAG = "RcViewOnTouchListener";

    private RecyclerView mRecyclerView;
    private GestureDetectorCompat mGestureDetector;
    private RcViewItemListener onItemClickListener;

    public RcViewOnTouchListener(final RecyclerView recyclerView,
                                 final RcViewItemListener onItemClickListener) {
        this.mRecyclerView = recyclerView;
        this.onItemClickListener = onItemClickListener;
        mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                        int position = mRecyclerView.getChildAdapterPosition(childView);
                        Log.i(TAG, "onSingleTapUp() position:" + position);
                        Toast.makeText(recyclerView.getContext(), "onSingleTapUp() position:" + position, Toast.LENGTH_SHORT).show();
                        if (childView != null && onItemClickListener != null) {
                            onItemClickListener.onClick(position, childView);
                        }
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                        int position = mRecyclerView.getChildAdapterPosition(childView);
                        Log.i(TAG, "onLongPress() position:" + position);
                        Toast.makeText(recyclerView.getContext(), "onLongPress() position:" + position, Toast.LENGTH_SHORT).show();
                        if (childView != null && onItemClickListener != null) {
                            onItemClickListener.onLongClick(position, childView);
                        }
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        return false;
    }
}
