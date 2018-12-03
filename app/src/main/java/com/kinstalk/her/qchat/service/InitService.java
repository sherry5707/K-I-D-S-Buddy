package com.kinstalk.her.qchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.kinstalk.her.qchat.voiceresponse.GiftVoiceResponseService;
import com.kinstalk.her.qchatcomm.utils.QAILog;

public class InitService extends Service {

    private static final String TAG = InitService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QAILog.w(TAG, "in onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        QAILog.w(TAG, "in onStartCommand");

        startVoiceResponseService();

        return START_STICKY;
    }

    private void startVoiceResponseService() {

        InitService.this.startService(new Intent(InitService.this, GiftVoiceResponseService.class));

        stopSelf();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QAILog.w(TAG, "in onDestroy");
    }
}
