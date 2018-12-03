package com.kinstalk.her.qchat.voiceresponse;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.qchat.ChatActivity;
import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.RemindActivity;
import com.kinstalk.her.qchat.activity.AIServiceCallback;
import com.kinstalk.her.qchat.activity.AIUtils;
import com.kinstalk.her.qchat.activity.KidsService;
import com.kinstalk.her.qchat.activity.NewBaseAIService;
import com.kinstalk.her.qchat.activityhelper.VoiceAssistantActivityHelper;
import com.kinstalk.her.qchat.common.AICoreCallbackInterface;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.qloveaicore.AIManager;
import com.tencent.xiaowei.info.QLoveResponseInfo;
import com.tencent.xiaowei.info.XWResponseInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class GiftVoiceResponseService extends Service implements AICoreCallbackInterface {

    private static final String TAG = GiftVoiceResponseService.class.getSimpleName();

    public static final String TYPE_GIFT = "gift";

    public static final String localSvcPkg = "com.kinstalk.her.qchat";
    public static final String localSvcCls = "kinstalk.her.qchat.voiceresponse.GiftVoiceResponseService";


    private VoiceAssistantActivityHelper helper;

    MyBroadcastReceiver mbcr;

    //bind 定义传递数据
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCallback(Object obj) {
        if (TextUtils.isEmpty(obj.toString()))
            return;
        QLoveResponseInfo rspData = (QLoveResponseInfo) obj;
        XWResponseInfo xwResponseInfo = rspData.xwResponseInfo;
        if (xwResponseInfo == null)
            return;
        Log.d(TAG, "QLoveResponseInfo -> " + rspData.toString());
        try {
            JSONObject object = new JSONObject(rspData.xwResponseInfo.responseData);
            String intentName = object.optString("intentName");
            if (!TextUtils.isEmpty(intentName) && (intentName.equals("wakeup_viewMessage") ||
                    intentName.equals("wakeup_sendMessage"))) {
                playText("好的");
                startChatActivity(this);
            } else if (!TextUtils.isEmpty(intentName) && intentName.equals("wakeup_openReminder")) {
                playText("好的");
                startRemindActivity(this);
            } else if (!TextUtils.isEmpty(intentName) && intentName.equals("wakeup_openGift")) {
//                playText("好的");
                startGiftActivity(this);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        QAILog.d(TAG, "GiftVoiceResponseService onCreate RUN ");

        helper = VoiceAssistantActivityHelper.instance;

        mbcr = new MyBroadcastReceiver();
        KidsService.addToCallback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("VOICE_RESPONSE_NEW_GIFT");
        registerReceiver(mbcr, filter);// 注册
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        QAILog.d(TAG, "GiftVoiceResponseService onStartCommand RUN ");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KidsService.removeFromCallback(this);
        unregisterReceiver(mbcr);

        QAILog.d(TAG, "GiftVoiceResponseService onDestroy RUN ");
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //对接收到的广播进行处理，intent里面包含数据

            String action = intent.getAction();

            QAILog.d(TAG, "GiftVoiceResponseService onReceive -> " + action);

            if ("VOICE_RESPONSE_NEW_GIFT".equals(action)) {

                int id = intent.getIntExtra("voice_id", 0);
//                String baby = getNickName();
                switch (id) {
                    case 0:
                        break;
                    case 801:
//                        if (helper == null) {
//                            helper = VoiceAssistantActivityHelper.instance;
//                        }
                        String str_content1 = "这里有好多的礼物";
//                        helper.playWorkContent(context, str_content1);

                        playText(str_content1);
                        startGiftActivity(context);
                        break;
                    case 802:

//                        if (helper == null) {
//                            helper = VoiceAssistantActivityHelper.instance;
//                        }
                        String str_content2 = "快来看看有都有什么礼物";
//                        helper.playWorkContent(context, str_content2);
                        playText(str_content2);
                        startGiftActivity(context);
                        break;
                    default:
                        break;
                }

            } else {

//                if (helper == null) {
//                    helper = VoiceAssistantActivityHelper.instance;
//                }
                String str_content = "礼物箱空空的";
//                helper.playWorkContent(context, str_content);
                playText(str_content);

            }

        }
    }

    private String getNickName() {

        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
        return (kidsEntity != null) ? kidsEntity.nick_name : "宝宝";
    }

    private void playText(String text) {
        playTTSWithContent(text);
    }

    public void playTTSWithContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGiftActivity(Context context) {
        Intent intent = new Intent(context, GiftActivity.class);
        intent.putExtra("page_number", 1);
        context.startActivity(intent);
    }

    private void startChatActivity(Context context) {
        Intent intent = new Intent(context, ChatActivity.class);
        context.startActivity(intent);
    }

    private void startRemindActivity(Context context) {
        Intent intent = new Intent(context, RemindActivity.class);
        context.startActivity(intent);
    }

}
