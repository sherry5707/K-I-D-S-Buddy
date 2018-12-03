package com.kinstalk.her.qchat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kinstalk.her.qchat.service.InitService;

import com.kinstalk.her.qchatcomm.utils.QAILog;

import com.kinstalk.her.qchat.voiceresponse.VoiceResponseService;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        LogRecorder.init(context);
//        QAILog.d("BootCompletedReceiver", "BootCompletedReceiver onReceive run");
//        context.startService(new Intent(context, InitService.class));
//        context.startService(new Intent(context, VoiceResponseService.class));
    }
}
