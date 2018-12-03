/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatcomm.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.util.List;

/**
 * Created by Knight.Xu on 2018/4/9.
 */
public class SDCardUtil {

    private static final String TAG = SDCardUtil.class.getSimpleName();

    /**
     * M: Whether the device has a mounded sdcard or not.
     * 
     * @param context a context.
     * @return If one of the sdcard is mounted, return true, otherwise return
     *         false.
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static boolean hasMountedSDcard(Context context) {
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        boolean hasMountedSDcard = false;
        if (storageManager != null) {
            List<StorageVolume> volumePaths = storageManager.getStorageVolumes();
            if (volumePaths != null) {
                for (StorageVolume storageVolume : volumePaths) {
                    String status = storageVolume.getState();
                    Log.d(TAG, "hasMountedSDcard: path = " + storageVolume + ",status = " + status);
                    if (Environment.MEDIA_MOUNTED.equals(status)) {
                        hasMountedSDcard = true;
                    }
                }
            }
        }
        Log.d(TAG, "---hasMountedSDcard = " + hasMountedSDcard);
        return hasMountedSDcard;
    }

    /**
     * 检查SD卡是否存在
     * 
     * @return boolean
     */
    public static boolean isSDMounted() {
        return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
    }

    /**
     * 对sdcard的检查，主要是检查sd是否可用，并且sd卡的存储空间是否充足
     *
     */
    public static boolean isSDEnough(long streamLength)  {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        } else {
            return streamLength >= getFreeSD();
        }
    }

    /**
     * 获取SD卡的剩余空间
     * 
     * @return SD卡的剩余的字节数
     */
    public static long getFreeSD() {
        long nAvailableCount = 0l;
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            nAvailableCount = (long) (stat.getBlockSize() * ((long) stat.getAvailableBlocks()));
        } catch (Exception e) {
        }
        return nAvailableCount;
    }

    
}
