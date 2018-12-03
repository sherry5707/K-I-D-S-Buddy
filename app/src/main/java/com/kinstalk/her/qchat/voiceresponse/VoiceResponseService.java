package com.kinstalk.her.qchat.voiceresponse;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.kinstalk.her.qchat.activity.AIServiceCallback;
import com.kinstalk.her.qchat.activity.AIUtils;
import com.kinstalk.her.qchat.activity.NewBaseAIService;
import com.kinstalk.her.qchat.translation.TranslationBean;
import com.kinstalk.her.qchat.translation.TranslationDBHelper;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.tencent.xiaowei.info.QLoveResponseInfo;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kinstalk.qloveaicore.AIManager;


public class VoiceResponseService extends Service {

    /**
     * TAG
     */
    private static final String TAG = VoiceResponseService.class.getSimpleName();

    MyBroadcastReceiver mbcr;

    HWBroadcastReceiver hwbr;

    SCBroadcastReceiver scbr;

//    TlBroadcastReceiver tlbr;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {

        public VoiceResponseService getService() {
            return VoiceResponseService.this;
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        QAILog.d(TAG, "VoiceResponseService onCreate RUN ");

//        mbcr = new MyBroadcastReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("VOICE_RESPONSE_TTS_CONTENT");
//        registerReceiver(mbcr, filter);// 注册

        hwbr = new HWBroadcastReceiver();
        IntentFilter hwfilter = new IntentFilter();
        hwfilter.addAction("VOICE_RESPONSE_NEW_HOMEWORK");
        registerReceiver(hwbr, hwfilter);// 注册

//        scbr = new SCBroadcastReceiver();
//        IntentFilter scfilter = new IntentFilter();
//        scfilter.addAction("VOICE_RESPONSE_SKILL_COMMAND");
//        registerReceiver(scbr, scfilter);

//        tlbr = new TlBroadcastReceiver();
//        IntentFilter tlfilter = new IntentFilter();
//        tlfilter.addAction("kingstalk.action.wateranimal.close");
//        tlfilter.addAction("kingstalk.action.wateranimal.senddatatokb");
//        registerReceiver(tlbr, tlfilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        QAILog.d(TAG, "VoiceResponseService onStartCommand RUN ");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mbcr);
        unregisterReceiver(hwbr);
//        unregisterReceiver(scbr);
//        unregisterReceiver(tlbr);
        QAILog.d(TAG, "VoiceResponseService onDestroy RUN ");
    }

    public void playText(String text) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(text, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通用
     */
    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //对接收到的广播进行处理，intent里面包含数据

            String action = intent.getAction();

            QAILog.d(TAG, "MyBroadcastReceiver onReceive -> " + action);

            if ("VOICE_RESPONSE_TTS_CONTENT".equals(action)) {

                String content = intent.getStringExtra("CONTENT");

                if (TextUtils.isEmpty(content)) {
                    playText("小薇刚才发呆了，没听清你说什么，在说一遍好不好？");
                } else {
                    playText(content);
                }

            }

        }
    }

    /**
     * 作业相关
     */
    class HWBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //对接收到的广播进行处理，intent里面包含数据
            int id = intent.getIntExtra("voice_id", 0);
            String baby = getNickName();
            switch (id) {
                case 0:
                    break;
                case 501:

                    String str_content1 = baby + "，你有新的作业，快来看看吧";
                    playText(str_content1);
                    break;
                case 502:
                    String str_content2 = baby + "，有作业喽";
                    playText(str_content2);

                    break;
                default:
                    break;
            }
        }
    }

    class SCBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            QAILog.d(TAG, "SCBroadcastReceiver onReceive -> " + intent.getStringExtra("SKILL_COMMAND"));

            String content = intent.getStringExtra("SKILL_COMMAND");

            if (TextUtils.isEmpty(content)) {
                playText("小薇刚才发呆了，没听清你说什么，在说一遍好不好？");
            } else {
                try {
                    AIManager.getInstance(getApplicationContext()).textRequest(content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private String getNickName() {

        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
        return (kidsEntity != null) ? kidsEntity.nick_name : "宝宝";
    }

}
