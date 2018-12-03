/*
 * Copyright (c) 2017. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatapi.helper;

import android.content.Context;
import android.text.TextUtils;

import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import java.util.Timer;
import java.util.TimerTask;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;

/**
 * Created by Knight.Xu on 2018/4/10.
 * 调用config接口
 */

public class ConfigApiHelper {

    private static final String TAG = "AI-ConfigApiHelper";

    private final Object fetchConfigLock = new Object();
    private static volatile boolean isConfigFetched = false;

    private Timer mFetchConfigTimer;
    private static final int FETCH_CONFIG_PERIOD = 1000 * 20;

    private static ConfigApiHelper sInstance;
    public static ConfigApiHelper getInstance() {
        if (sInstance == null) {
            sInstance =  new ConfigApiHelper();
        }
        return sInstance;
    }

    private ConfigApiHelper() {
    }

    public void startReqConfigTimer(final Context context) {
        if (mFetchConfigTimer == null) {
            mFetchConfigTimer = new Timer();
            mFetchConfigTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    QAILog.d(TAG, "mFetchConfigTimer: run");
                    fetchConfigList(context, QAIConfig.CONFIG_ACTION_ALL);
                }
            }, 0, FETCH_CONFIG_PERIOD);
        }
    }

    public void changeConfig(final Context context, final String action) {
        synchronized (fetchConfigLock) {
            QAILog.d(TAG, "newFetchConfig: enter");
            isConfigFetched = false;
            QchatThreadManager.getInstance().start(new Runnable() {
                @Override
                public void run() {
                    QAILog.d(TAG, "newFetchConfig: run");
                    fetchConfigList(context, action);
                }
            });
        }
    }

    public void tryFetchConfig(final Context context) {
        synchronized (fetchConfigLock) {
            QAILog.d(TAG, "tryFetchConfig: enter");
            if (!isConfigFetched) {
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        QAILog.d(TAG, "tryFetchConfig: run");
                        fetchConfigList(context, QAIConfig.CONFIG_ACTION_ALL);
                    }
                });
            }
        }
    }

    private synchronized void fetchConfigList(Context context, String action) {
        synchronized (fetchConfigLock) {
            if (!isConfigFetched) {
                QAILog.d(TAG, "fetchConfigList: Enter");
                try {
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        // 准备sn
                        Api.reqConfigList(context, action);
                        if (!TextUtils.isEmpty(QAIConfig.accesskey_url)){
                            QAILog.d(TAG, "fetchConfigList: Fetched");
                            isConfigFetched = true;
                            if (mFetchConfigTimer != null) {
                                mFetchConfigTimer.cancel();
                                mFetchConfigTimer = null;
                            }
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    QAILog.e(TAG, "fetchConfigList: Exception: " + t.getMessage());
                }
            }
        }
    }
}
