package com.binzosoft.lib.file_manager;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerViewItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {

    public interface OnItemClickListener {
        void onItemClick(View view);

        void onItemLongClick(View view);
    }

    private GestureDetectorCompat mGestureDetector;

    public RecyclerViewItemTouchListener(final RecyclerView recyclerView,
                                         final OnItemClickListener onItemClickListener) {
        mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null && onItemClickListener != null) {
                            onItemClickListener.onItemClick(childView);
                        }
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null && onItemClickListener != null) {
                            onItemClickListener.onItemLongClick(childView);
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