package com.kinstalk.her.qchat.voiceresponse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.kinstalk.her.qchat.ChatActivity;
import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.activity.AIServiceCallback;
import com.kinstalk.her.qchat.activity.AIUtils;
import com.kinstalk.her.qchat.activity.NewBaseAIService;
import com.kinstalk.her.qchat.activityhelper.VoiceAssistantActivityHelper;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.tencent.xiaowei.info.QLoveResponseInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class MSGVoiceResponseService extends NewBaseAIService {

    private static final String TAG = MSGVoiceResponseService.class.getSimpleName();

    public static final String TYPE_MESSAGE = "message";

    public static final String localSvcPkg = "com.kinstalk.her.qchat";
    public static final String localSvcCls = "kinstalk.her.qchat.voiceresponse.MSGVoiceResponseService";

    private MSGVoiceResponseService.AIResultBinder resultBinder = new MSGVoiceResponseService.AIResultBinder();

    private AIServiceCallback callback = null;

    MyBroadcastReceiver mbcr;

    @Override
    public String getBindParams() {
        return AIUtils.buildJson(TYPE_MESSAGE, localSvcPkg, localSvcCls);
    }

    @Override
    public void onJsonResult(String jsonResult) {
        QAILog.d(TAG, "MSGVoiceResponseService onJsonResult -> jsonResult -> " + jsonResult);
        try {
            JSONObject jsonObject = new JSONObject(jsonResult);

            if (callback != null && (jsonObject instanceof JSONObject)) {
                String text = jsonObject.getString("text");
                if (text.equals("我要发消息")) {
                    playText("好的");
                } else if (text.equals("我要看消息")) {
                    playText("好的");
                } else if (text.equals("打开聊天")) {
                    playText("好的");
                } else if (text.equals("聊天")) {
                    playText("好的");
                } else if (text.equals("打开消息箱")) {
                    playText("好的");
                } else if (text.equals("打开消息")) {
                    playText("好的");
                } else if (text.equals("消息")) {
                    playText("好的");
                }

                startChatActivity(MSGVoiceResponseService.this);
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

    //bind 定义传递数据
    @Override
    public IBinder onBind(Intent intent) {
        return resultBinder;
    }

    public class AIResultBinder extends Binder {
        public MSGVoiceResponseService getService() {
            return MSGVoiceResponseService.this;
        }
    }

    public void setCallback(AIServiceCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        QAILog.d(TAG, "VoiceResponseService onCreate RUN ");

        mbcr = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("VOICE_RESPONSE_NEW_MESSAGE");
        registerReceiver(mbcr, filter);// 注册
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
        unregisterReceiver(mbcr);
        QAILog.d(TAG, "VoiceResponseService onDestroy RUN ");
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //对接收到的广播进行处理，intent里面包含数据
            int id = intent.getIntExtra("voice_id", 0);
            String baby = getNickName();
            switch (id) {
                case 0:
                    break;
                case 601:
                    String str_content1 = baby + "，你有新的消息，快来看看吧";
                    playText(str_content1);
                    break;
                case 602:
                    String str_content2 = baby + "，有消息喽";
                    playText(str_content2);
                    break;

                case 603:
                    String str_content3 = baby + "，快来看看妈妈说了什么";
                    playText(str_content3);
                    break;
                default:
                    break;
            }
        }
    }

    private void playText(String text) {
        if (getAImanager() != null) {
            try {
                getAImanager().ttsSpeakText(text);
            } catch (Exception e) {
            }
        }
    }

    private String getNickName() {

        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
        return (kidsEntity != null) ? kidsEntity.nick_name : "宝宝";
    }

    private void startChatActivity(Context context) {
        Intent intent = new Intent(context, ChatActivity.class);
        context.startActivity(intent);
    }

}
