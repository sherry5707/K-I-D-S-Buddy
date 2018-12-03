/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatcomm.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by Knight.Xu on 2018/4/6.
 */

public class BaseApplication extends Application {
    private static BaseApplication sInstance;
    public static Context mApplication;


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mApplication = this.getApplicationContext();
    }

    public static BaseApplication getInstance() {
        return sInstance;
    }

}
