package com.kinstalk.her.qchat.activity;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;

/**
 * Created by siqing on 17/4/21.
 */

public class LockManager {
    public static LockManager _instance;
    private PowerManager pm;

    private PowerManager.WakeLock unLock;

    private Context mContext;

    private LockManager(Context context) {
        this.mContext = context.getApplicationContext();
        pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    }

    public static LockManager getInstance(Context context) {
        if (_instance == null) {
            synchronized (LockManager.class) {
                if (_instance == null) {
                    _instance = new LockManager(context);
                }
            }
        }
        return _instance;
    }

    private PowerManager.WakeLock getWakeLock() {
        releaseWakeLock();
        if (unLock == null) {
            unLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "bright");// 得到键盘锁管理器对象
        }
        return unLock;
    }

    /**
     * 释放屏幕锁
     */
    public void releaseWakeLock() {
        if (unLock != null) {
            unLock.release();
            unLock = null;
        }
    }

    /**
     * 点亮屏幕
     */
    public void requireScreenOn() {
        getWakeLock().acquire();
    }


}
