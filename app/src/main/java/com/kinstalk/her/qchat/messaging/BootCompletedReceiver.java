package com.kinstalk.her.qchat.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kinstalk.her.qchatcomm.utils.QAILog;

public class BootCompletedReceiver extends BroadcastReceiver {
    String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        QAILog.d(TAG, "onReceive: BootCompletedReceiver");
    }
}
