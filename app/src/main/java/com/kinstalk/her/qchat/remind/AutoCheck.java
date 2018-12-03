package com.kinstalk.her.qchat.remind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.messaging.QchatBroadcast;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import java.util.Calendar;

/**
 * Created by Tracy on 2018/4/19.
 */

public class AutoCheck {
    private static String TAG = "AutoCheck";
    public static final int AUTOCHECK_PENDING_INTENT_ID = 11;
    public static final int AUTOCHECK_QRCODE_PENDING_INTENT_ID = 12;

    public static void registerCheckInNight(Context context)
    {
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        QAILog.i(TAG, "set homework clear check at :" + UIHelper.DateFormat(calendar.getTimeInMillis()));
        setAlarm(context, calendar.getTimeInMillis(), true);
    }
    /**
     * 设置提醒发送查询请求
     *
     * @param context
     * @param trigerTime
     * @param doSave
     */
    public static void setAlarm(Context context, long trigerTime, boolean doSave) {
        Intent updIntent = new Intent(UIHelper.ACTION_CLEAR_HOME);
        updIntent.setClass(context, QchatBroadcast.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, AUTOCHECK_PENDING_INTENT_ID, updIntent, 0);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // set alarm
        am.set(AlarmManager.RTC_WAKEUP, trigerTime, pending);
        QAILog.i(TAG, "setAlarm over time is:" + UIHelper.DateFormat(trigerTime));
        if (doSave) {
            // save alarm time
            SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFERENCE_NAME_UI,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("alarm_time", trigerTime);
            editor.commit();
        }
    }

    public static void cancelAlarm(Context context) {
        Intent updIntent = new Intent(UIHelper.ACTION_CLEAR_HOME);
        updIntent.setClass(context, QchatBroadcast.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, AUTOCHECK_PENDING_INTENT_ID, updIntent, 0);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // set alarm
        am.cancel(pending);
    }

    public static void registerQRCodeRefresh(Context context)
    {
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, 55);
        calendar.set(Calendar.SECOND, 0);

        QAILog.i(TAG, "set QRCode refresh check at :" + UIHelper.DateFormat(calendar.getTimeInMillis()));
        setQRCodeAlarm(context, calendar.getTimeInMillis(), true);
    }

    private static void setQRCodeAlarm(Context context, long trigerTime, boolean doSave) {
        Intent updIntent = new Intent(UIHelper.QRCODE_CHECK);
        updIntent.setClass(context, QchatBroadcast.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, AUTOCHECK_QRCODE_PENDING_INTENT_ID, updIntent, 0);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // set alarm
        am.set(AlarmManager.RTC_WAKEUP, trigerTime, pending);
        QAILog.i(TAG, "setQRCodeAlarm over time is:" + UIHelper.DateFormat(trigerTime));
        if (doSave) {
            // save alarm time
            SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFERENCE_NAME_UI,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("qrcode_alarm_time", trigerTime);
            editor.commit();
        }
    }

    public static void cancelQRCodeAlarm(Context context) {
        Intent qrdIntent = new Intent(UIHelper.QRCODE_CHECK);
        qrdIntent.setClass(context, QchatBroadcast.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, AUTOCHECK_QRCODE_PENDING_INTENT_ID, qrdIntent, 0);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // set alarm
        am.cancel(pending);
    }
}

