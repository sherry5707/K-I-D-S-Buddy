package com.kinstalk.her.qchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import com.kinstalk.util.AppUtils;

/**
 * Created by Tracy on 2018/4/23.
 * partially copy from Reminder
 */
public class AlarmBaseActivity extends AppCompatActivity {

    private static final long CLICK_REPEATTIME = 500;

    private long lastClickTime = 0;
    private int lastClickViewId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(com.kinstalk.her.qchat.R.style.ThemeNight);
        super.onCreate(savedInstanceState);

        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 用于判断是否是重复点击
     *
     * @param view
     * @return
     */
    protected boolean isRepeatClick(View view) {
        boolean isRepeatClick = false;

        if (view == null) {
            return false;
        }

        if (lastClickViewId == 0) {
            lastClickViewId = view.getId();
            lastClickTime = System.currentTimeMillis();
            return false;
        }

        int tmpClickViewId = view.getId();
        long tmpClickTime = System.currentTimeMillis();

        if (tmpClickViewId == lastClickViewId) {
            if (tmpClickTime - lastClickTime <= CLICK_REPEATTIME) {
                isRepeatClick = true;
            }

            lastClickTime = tmpClickTime;
        } else {
            if (tmpClickTime - lastClickTime <= CLICK_REPEATTIME) {
                isRepeatClick = true;
            }

            lastClickTime = tmpClickTime;
            lastClickViewId = tmpClickViewId;
        }

        return isRepeatClick;
    }

    /**
     * 切home,通知清空activity,一般用作手动点击home按钮
     */
    public void switchLauncher() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    /**
     * 取消自动回到首页方法
     *
     * @param auto true 30s自动回到首页 false 取消自动回到首页
     */
    public void setAutoSwitchLauncher(boolean auto) {
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), auto);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


}