package com.kinstalk.her.qchat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kinstalk.her.qchat.utils.OnMultiTouchListener;

import java.util.concurrent.atomic.AtomicInteger;

public class DoubleClickLinearLayout extends LinearLayout {

    private final String TAG = "DoubleClickLinearLayout";

    private OnDoubleClickListener onDoubleClickListener;
    private long lastTouchTime = 0;
    private AtomicInteger touchCount = new AtomicInteger(0);

    private Runnable mRun = null;

    //自定义监听器接口
    public interface OnDoubleClickListener {
        void onDoubleClick(ViewGroup view);
    }

    //设置双击事件监听器的方法
    public void setOnDoubleClickListener(OnDoubleClickListener onDoubleClickListener) {
        this.onDoubleClickListener = onDoubleClickListener;
    }

    public DoubleClickLinearLayout(Context context) {
        super(context);
    }

    public DoubleClickLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "DoubleClickLinearLayout: attrs");
        this.setOnTouchListener(new OnMultiTouchListener() {
            @Override
            public void onMultiTouch(View v, MotionEvent event, int touchCount) {
                Log.d(TAG, "onMultiTouch: " + touchCount + v);
                if (touchCount == 2 && onDoubleClickListener != null) {
                    onDoubleClickListener.onDoubleClick(DoubleClickLinearLayout.this);
                }
            }
        });
    }
}
