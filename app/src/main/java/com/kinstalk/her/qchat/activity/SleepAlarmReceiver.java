package com.kinstalk.her.qchat.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchatcomm.utils.CommonDefUtils;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;

import static com.kinstalk.her.qchat.activity.AIBaseActivity.stopSleepOrWakeup;
import static com.kinstalk.her.qchat.common.UIHelper.wchatBindStatus;
import static com.kinstalk.her.qchat.common.UIHelper.xiaoweiBindStatus;

public class SleepAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SleepAlarmReceiver", "intent:" + intent);

        String action = intent.getAction();
        if (CommonDefUtils.KINSTALK_QCHAT_BIND_STATUS.equals(action)) {
            boolean bind_status = intent.getBooleanExtra("qchat.bind_status", true);
            if (!bind_status) {
                stopPlay(context);
            }
        } else if (CommonDefUtils.KINSTALK_AICORE_BIND_STATUS.equals(action)) {
            boolean bind_status = intent.getBooleanExtra("bind_status", true);
            if (!bind_status) {
//                stopPlay(context);
                stopSleepOrWakeup();
            }

        } else if(AIConstants.SLEEP_ALARM_ACTION.equals(action)){
            sleepActionStart(context);
        }else if(AIConstants.WAKEUP_ALARM_ACTION.equals(action)){
            wakeupActionStart(context);
        }else if(AIConstants.SLEEP_REMOVE_PRIVACY_ACTION.equals(action)){
            removePrivacy(context);
        }
    }

    private void stopPlay(Context context) {
        AIBaseActivity.stopPlay(context);
    }

    private void sleepActionStart(Context context) {

        if(QchatApplication.isSleepingActive()) {
            SleepActivity.actionActivite(context);
        }else{
            SleepModeActivity.actionActivite(context);
        }
    }

    private void wakeupActionStart(Context context) {
        WakeUpActivity.actionActivite(context);
    }

    protected void removePrivacy(Context context) {
        try {
            com.kinstalk.util.AppUtils.setPrivacyMode(context, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
