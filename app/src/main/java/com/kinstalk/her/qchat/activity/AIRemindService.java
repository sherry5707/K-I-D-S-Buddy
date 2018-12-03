package com.kinstalk.her.qchat.activity;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tencent.xiaowei.info.QLoveResponseInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangyong on 18/5/23.
 */

public class AIRemindService extends NewBaseAIService {
    private static final String TAG = "HerSleepOrWakeUp";

    public static final String type = "sleepmode";
    public static final String localSvcPkg = "com.kinstalk.her.qchat";
    public static final String localSvcCls = "kinstalk.com.her.qchat.activity.AIService";


    private AIResultBinder resultBinder = new AIResultBinder();
    private AIServiceCallback callback = null;

    @Override
    public String getBindParams() {
        return AIUtils.buildJson(type, localSvcPkg, localSvcCls);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void startPlayStory(final String cmd) {

    }

    @Override
    public void onJsonResult(String jsonResult) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);
            if (callback != null && jsonObject != null && (jsonObject instanceof JSONObject)) {
                String service = jsonObject.getString("intentName");
                if (service == null) {
                    return;
                }
                if (service.equals("wakeup_checkout")) {
                    callback.onJsonResult(jsonObject);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleQLoveResponseInfo(String voiceId, QLoveResponseInfo rspData, byte[] extendData) {

    }

    @Override
    public void handleWakeupEvent(int command, String data) {

    }

    public void playTTSText(String text) {

        if (getAImanager() != null) {
            try {
                getAImanager().ttsSpeakText(text);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onAIServiceDisConnected() {
    }

    //bind 定义传递数据
    @Override
    public IBinder onBind(Intent intent) {
        return resultBinder;
    }

    public class AIResultBinder extends Binder {
        public AIRemindService getService() {
            return AIRemindService.this;
        }
    }

    public void setCallback(AIServiceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
