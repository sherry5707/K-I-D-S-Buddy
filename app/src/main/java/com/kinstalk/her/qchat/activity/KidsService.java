package com.kinstalk.her.qchat.activity;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kinstalk.her.qchat.common.AICoreCallbackInterface;
import com.tencent.xiaowei.info.QLoveResponseInfo;

import java.util.ArrayList;

import kinstalk.com.qloveaicore.AICoreDef;

/**
 * Created by Tracy on 18/8/8.
 */

public class KidsService extends IntentService {
    private static final String TAG = "KidsService";

    public static final String type = "sleepmode";
    public static final String localSvcPkg = "com.kinstalk.her.qchat";
    public static final String localSvcCls = "kinstalk.com.her.qchat.activity.AIService";
    private static ArrayList<AICoreCallbackInterface> mCallback = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public KidsService(String name) {
        super("KidsService");
        Log.d(TAG,"KidService");
    }

    public KidsService() {
        super("KidsService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (null == intent) {
            return;
        }
        Log.d(TAG, "onHandleIntent");
        toDoWithCommand(intent);
    }

    private void toDoWithCommand(Intent intent) {
        if (intent != null) {
            QLoveResponseInfo rspData = intent.getParcelableExtra(AICoreDef.AI_INTENT_SERVICE_INFO);
            byte[] extendData = intent.getByteArrayExtra(AICoreDef.AI_INTENT_SERVICE_DATA);

            handleQLoveResponseInfo( rspData, extendData);
        }
    }

    public void handleQLoveResponseInfo(QLoveResponseInfo rspData, byte[] extendData) {
  //      QAIKidsConvertor.getInstance().handleQLoveResponseInfo(rspData, extendData);
        notifyToAllCallback(rspData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void addToCallback(AICoreCallbackInterface itf) {
        if(!mCallback.contains(itf)) {
            mCallback.add(itf);
        }
    }

    public static void removeFromCallback(AICoreCallbackInterface itf) {
        if(!mCallback.isEmpty() && mCallback.contains(itf)) {
            mCallback.remove(itf);
        }
    }

    private void notifyToAllCallback(QLoveResponseInfo info) {
        if(!mCallback.isEmpty()) {
            for(int i = 0; i< mCallback.size(); i++)
                mCallback.get(i).onCallback(info);
        }
    }
}