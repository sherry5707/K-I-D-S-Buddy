package com.kinstalk.her.qchat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.kinstalk.her.qchatcomm.base.BaseApplication;

public class SharedPreferencesUtils {

    public static final String WORK_TIME = "work_time";             // 新作业的提醒（已读后的时间）
    public static final String WORK_ALERT_TIME = "work_alert_time";
    public static final String WORK_FINISH = "work_finish";         // 作业完成标记
    public static final String WORK_ID_FINISH = "work_id_finish";
    public static final String TTS_CONTENT = "tts_content";
    public static final String HAS_INSERT = "has_insert";

    public static void setSharedPreferencesWithLong(Context context, String name, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value).commit();
    }

    public static long getSharedPreferencesWithLong(Context context, String name, String key, long defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        if (preferences.contains(key)) {
            return preferences.getLong(key, defaultValue);
        }
        return (long)defaultValue;
    }

    public static void setSharedPreferencesWithBool(Context context, String name, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value).commit();
    }

    public static boolean getSharedPreferencesWithBool(Context context, String name, String key, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        if (preferences.contains(key)) {
            return preferences.getBoolean(key, defaultValue);
        }
        return false;
    }

    public static boolean containsSharedPreferences(Context context, String name, String key) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return preferences.contains(key);
    }

    public static void clearSharedPreferences(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }
}
