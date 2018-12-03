package com.kinstalk.her.qchat.receive;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;

/**
 * 用户询问百科之后，发送广播，我请求服务器完成百科任务
 * Created by bean on 2018/7/9.
 */

public class TaskReceive extends BroadcastReceiver {
    private String sn = QAIConfig.getMacForSn();
    private String token = StringEncryption.generateToken();

    @Override
    public void onReceive(final Context context, Intent intent) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Api.postReceiveTask(context, token, sn, -1, -1, 1, "got");
            }
        });
    }
}

