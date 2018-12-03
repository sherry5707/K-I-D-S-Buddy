package com.kinstalk.her.qchat.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class OnMultiTouchListener implements View.OnTouchListener {

    /**
     * 上次 onTouch 发生的时间
     */
    private long lastTouchTime = 0;
    /**
     * 已经连续 touch 的次数
     */
    private AtomicInteger touchCount = new AtomicInteger(0);

    private Runnable mRun = null;

    public void removeCallback() {
        if (mRun != null) {
            mHandler.removeCallbacks(mRun);
            mRun = null;
        }
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final long now = System.currentTimeMillis();
            lastTouchTime = now;

            touchCount.incrementAndGet();
            //每点击一次就移除上一次的延迟任务，重新布置一个延迟任务
            removeCallback();

            mRun = new Runnable() {
                @Override
                public void run() {
                    //两个变量相等,表示时隔 multiTouchInterval之后没有新的touch产生, 触发事件并重置touchCount
                    if (now == lastTouchTime) {
                        onMultiTouch(v, event, touchCount.get());
                        touchCount.set(0);
                    }
                }
            };

            postTaskInUIThread(mRun, getMultiTouchInterval());
        }
        return true;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void postTaskInUIThread(Runnable r, long delayMillis) {
        mHandler.postDelayed(r, delayMillis);
    }


    /**
     * 连续touch的最大间隔, 超过该间隔将视为一次新的touch开始， 默认是400，推荐值，也可以由客户代码指定
     *
     * @return
     */
    protected int getMultiTouchInterval() {
        return 400;
    }

    /**
     * 连续点击事件回调
     *
     * @param v
     * @param event
     * @param touchCount 连续点击的次数
     * @return
     */
    public abstract void onMultiTouch(View v, MotionEvent event, int touchCount);
}
