package com.kinstalk.her.qchat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;

public class SleepModeActivity extends AIBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_mode);

        playTtsText(" ");
        stopStories();
        QchatApplication.getApplicationInstance().setSleepActive(true);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchPrivacy(true);
                gotoSleep();
                finish();
            }
        }, 3000);
    }

    public static void actionActivite(Context context) {
        Intent intent = new Intent(context, SleepModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Bundle bundle = new Bundle();
        bundle.putBoolean("msleep", true);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
