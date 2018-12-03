package com.kinstalk.her.qchat.activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.RemindCustomActivity;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.qloveaicore.CmdCallback;
import com.kinstalk.qloveaicore.TTSListener;
import com.tencent.xiaowei.info.QControlCmdInfo;
import com.tencent.xiaowei.info.QLoveResponseInfo;
import com.tencent.xiaowei.info.XWResponseInfo;

import org.json.JSONObject;


/**
 * Created by Tracy on 2018/8/8.
 */


public class QAIKidsConvertor {
    private static final String TAG = "QAIKidsConvertor";

    private static volatile QAIKidsConvertor mInstance;

    private Context mContext;


    public static void registerJsonReceiver() {

    }

    public static void unregisterJsonReceiver() {

    }

    private static TTSListener mCommonTTSCb = new TTSListener() {

        @Override
        public void onTTSPlayBegin(String s) {

        }

        @Override
        public void onTTSPlayEnd(String s) {
            Log.d(TAG, "onTTSPlayEnd");
        }

        @Override
        public void onTTSPlayProgress(String s, int i) {

        }

        @Override
        public void onTTSPlayError(String s, int i, String s1) {
            Log.d(TAG, "onTTSPlayError");
        }
    };

    private TTSListener mIntroductionTTSCb = new TTSListener() {

        @Override
        public void onTTSPlayBegin(String s) {

        }

        @Override
        public void onTTSPlayEnd(String s) {

        }

        @Override
        public void onTTSPlayProgress(String s, int i) {

        }

        @Override
        public void onTTSPlayError(String s, int i, String s1) {

        }
    };

    private static CmdCallback mMoreCmdCb = new CmdCallback(){
        @Override
        public void handleQLoveResponseInfo(String s, QLoveResponseInfo qLoveResponseInfo, byte[] bytes)  {
            Log.d(TAG, "more ResponseInfo: " + qLoveResponseInfo.toString());
            }
    };

    private QAIKidsConvertor(){
        mContext = QchatApplication.getApplicationInstance();
    }

    public static QAIKidsConvertor getInstance() {
        if (mInstance == null) {
            synchronized (QAIKidsConvertor.class) {
                if (mInstance == null) {
                    mInstance = new QAIKidsConvertor();
                }
            }
        }
        return mInstance;
    }

    public void handleQLoveResponseInfo(QLoveResponseInfo rspData, byte[] extendData) {
        if(TextUtils.isEmpty(rspData.toString()))
            return;
        XWResponseInfo xwResponseInfo = rspData.xwResponseInfo;
        if (xwResponseInfo == null)
            return;
        Log.d(TAG, "QLoveResponseInfo" +rspData.toString());
        try {
            JSONObject object = new JSONObject(rspData.xwResponseInfo.responseData);
            String intentName = object.optString("intentName");
            if(!TextUtils.isEmpty(intentName) && intentName.equals("wakeup_checkout")) {

            }
        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}