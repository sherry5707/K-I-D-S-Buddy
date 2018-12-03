package com.kinstalk.her.qchatmodel.Manager.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;

import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import org.json.JSONObject;

import com.kinstalk.util.AppUtils;

/**
 * Created by Tracy on 2018/5/24.
 */

public class UIHelp {

    private static final String TAG = "UIHelp";

    public static String SHARED_PREFERENCE_NAME_UI = "com.kinstalk.her.qchat.update_preferences_ui";
    public static String SHARED_PREFERENCE_NAME_TIME = "com.kinstalk.her.qchat.update_time";
    private static final String newWorkAction = "com.kinstalk.her.qchat.homework";
    private static final String newEnterWorkAction = "com.kinstalk.her.qchat.enter.homework";
    private static final String newMessageAction = "com.kinstalk.her.qchat.message";
    private static final String newEnterMessageAction = "com.kinstalk.her.qchat.enter.message";
    private static final String starAction = "com.kinstalk.her.qchat.star";
    private static final String remindAction = "com.kinstalk.her.qchat.reminder";
    private static final String star_extra = "all_star";
    private static final String type_extra = "type";
    private static final String time_extra = "time";

    private static final String sleepSign = "SLEEP_SIGN";
    private static final String getupSign = "GETUP_SIGN";
    private static final String workSign = "WORK_SIGN";
    private static final String updateTimeKey = "UPDATE_TIME";
    private static final int TYPE_GETUP = 3;
    private static final int TYPE_SLEEP = 4;
    private static final int TYPE_WORK = 5;
    private static final int TYPE_SELFHABIT = 6;

    private static boolean bWakeuping = false;
    private static boolean bSleeping = false;
    private static boolean bPreSleeping = false;
    //   private static Long time = 22L;
//    private static int star = 0;
    //   private static String already_post = "{\"time\":\"" +time +"\", \"star\":\"" +star +"\"}";


    public static void clearSharedPreferences(Context context, String name) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }
    public static void setWakeupActive(boolean active) {
        bWakeuping = active;
    }

    public static void setSleepActive(boolean active) {
        bSleeping = active;
    }

    public static void setPreSleepActive(boolean active) {
        bPreSleeping = active;
    }

    public static boolean isWakeupActive() {
        return bWakeuping;
    }

    public static boolean isSleepingActive() {
        return (bSleeping || bPreSleeping);
    }

    public static void setChildNameToSettings(Context context, String name) {
        Settings.System.putString(context.getContentResolver(),"__child_name__",name);
    }

    public static void setSelfDefineHabitAlarming(Context context, int status) {
        Settings.System.putInt(context.getContentResolver(),"__habit_alarm__",status);
    }

    private static boolean xiaoweiBindStatus(Context context) {
        return (1 == Settings.Secure.getInt(context.getContentResolver(), "xiaowei_bind_status", 0));
    }

    public static boolean isTopActivity2(String pkgName) {
        try {
            boolean is = AppUtils.isTopAcitivity(pkgName);
            QAILog.e(TAG, "isTopActivity " + pkgName + " " + is);
            return is;
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
        return false;
    }

    public static void showWorkAlert(Context context, String content, String id, int type) {
        QAILog.d(TAG, "showWorkAlert: content" + content);
        try {
            if (!xiaoweiBindStatus(context))
                return;
            WorkAlertInfo workAlertInfo = new WorkAlertInfo();
            workAlertInfo.setWorkString(content);
            workAlertInfo.setId(id);
            workAlertInfo.setType(type);

            Intent i = new Intent();
            i.setClassName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.homework.WorkAlertActivity");
            i.putExtra("WorkContent", workAlertInfo);
            context.startActivity(i);
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }

    public static void clearWorkAlert(Context context) {
        Intent i = new Intent();
        i.setClassName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.homework.WorkAlertActivity");
        i.putExtra("clear", true);
        context.startActivity(i);
    }

    public static void showWorkAlert(Context context, String content, String id) {
        showWorkAlert(context, content, id, 1);
    }

    public static void notifyWorkAlert(Context context, String id) {
        if (isTopActivityWorkAlert()) {
            showWorkAlert(context, null, id, 0);
        }
    }

    public static boolean isTopActivityWorkAlert() {
        ComponentName name = new ComponentName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.homework.WorkAlertActivity");
        return isTopActivity(name);
    }

    public static boolean isTopActivityWorkActivity() {
        ComponentName name = new ComponentName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.WorkActivity");
        return isTopActivity(name);
    }

    public static boolean isTopActivityChatActivity() {
        ComponentName name = new ComponentName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.ChatActivity");
        return isTopActivity(name);
    }

    public static boolean isTopActivity(ComponentName name) {
        try {
            boolean is = AppUtils.isTopAcitivity(name);
            QAILog.e(TAG, "isTopActivity " + name + " " + is);
            return is;
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
        return false;
    }

    public static void wakeUp(Context ctx) {
        try {
            AppUtils.wakeupDevice(ctx, SystemClock.uptimeMillis());
        } catch (Exception e) {
        }
    }

    public static void sendNewHomeworkBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(newWorkAction);
        context.sendStickyBroadcast(intent);
    }

    public static void sendEnterHomeworkBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(newEnterWorkAction);
        context.sendStickyBroadcast(intent);
    }

    public static void sendEnterMessageBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(newEnterMessageAction);
        context.sendStickyBroadcast(intent);
    }

    public static void sendNewMessageBroadcast(Context context) {
        if (isTopActivityChatActivity())
            return;
        Intent intent = new Intent();
        intent.setAction(newMessageAction);
        context.sendStickyBroadcast(intent);
    }

    public static void sendStarUpdateBroadcast(Context context, int star) {
        Intent intent = new Intent();
        intent.setAction(starAction);
        intent.putExtra(star_extra, star);
        context.sendStickyBroadcast(intent);
    }

    public static void sendReminderUpdateBroadcast(Context context, int type, Long time) {
        Intent intent = new Intent();
        intent.setAction(remindAction);
        intent.putExtra(type_extra, type);
        intent.putExtra(time_extra, time);
        context.sendStickyBroadcast(intent);
    }


    public static int jsonParseGetStar(String str) {
        try {
            JSONObject hello = new JSONObject(str);
            Long time = hello.getLong("time");
            int star = hello.getInt("star");
            QAILog.d(TAG, "jsonParse" + time + "star " + star);
            return star;
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
        return 0;
    }

    public static Long jsonParseGetTime(String str) {
        try {
            JSONObject hello = new JSONObject(str);
            Long time = hello.getLong("time");
            int star = hello.getInt("star");
            QAILog.d(TAG, "jsonParse" + time + "star " + star);
            return time;
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
        return 0L;
    }

    //  判断起床有无打卡
    public static int checkGetupSignOrNot(Context context) {
        return checkStatus(context, getupSign);
    }

    public static void setAlreadyGetupSign(Context context, int star) {
        setStatus(context, getupSign, star);
    }

    //判断睡觉有无打卡
    public static int checkSleepSignOrNot(Context context) {
        return checkStatus(context, sleepSign);
    }

    public static void setAlreadySleepSign(Context context, int star) {
        setStatus(context, sleepSign, star);
    }

    //判断自定义习惯有无打卡
    public static int checkHabitSignOrNot(Context context, int type) {
        if (type == TYPE_GETUP)
            return checkGetupSignOrNot(context);
        if (type == TYPE_SLEEP)
            return checkSleepSignOrNot(context);
        if (type == TYPE_WORK)
            return checkWorkSignOrNot(context);
        return checkStatus(context, Integer.toString(type));
    }

    //判断做作业有无打卡
    public static int checkWorkSignOrNot(Context context) {
        return checkStatus(context, workSign);
    }

    public static void setAlreadWorkSign(Context context, int star) {
        setStatus(context, workSign, star);
    }

    public static void setAlreadyHabitSign(Context context, int type, int star) {
        setStatus(context, Integer.toString(type), star);
    }

    public static void setAlreadyHabitSign(Context context, int type, int star, Long time) {
        try {
            if (checkHabitSignOrNot(context, type) == -1) {
                String sType = Integer.toString(type);
                if (TYPE_GETUP == type) {
                    sType = getupSign;
                } else if (TYPE_SLEEP == type) {
                    sType = sleepSign;
                } else if (TYPE_WORK == type) {
                    sType = workSign;
                }
                setStatus(context, sType, star, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAlreadySign(Context context, int type, int star) {
        if (TYPE_GETUP == type) {
            setAlreadyGetupSign(context, star);
        } else if (TYPE_SLEEP == type) {
            setAlreadySleepSign(context, star);
        } else if (TYPE_WORK == type) {
            setAlreadWorkSign(context, star);
        } else if (type > TYPE_WORK) {
            setAlreadyHabitSign(context, type, star);
        }
    }

    public static int checkSignOrNot(Context context, int type) {
        if (TYPE_GETUP == type) {
            return checkGetupSignOrNot(context);
        } else if (TYPE_SLEEP == type) {
            return checkSleepSignOrNot(context);
        } else if (TYPE_WORK == type) {
            return checkWorkSignOrNot(context);
        } else if (type > TYPE_WORK) {
            return checkHabitSignOrNot(context, type);
        }
        return -1;
    }

    private static int checkStatus(Context context, String key) {
        Long time = 0L;
        QAILog.d(TAG, "checkStatus key " +key);
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME_UI,
                Context.MODE_PRIVATE);
        String sign = sp.getString(key, "");
        if (!TextUtils.isEmpty(sign)) {
            int star = jsonParseGetStar(sign);
            time = jsonParseGetTime(sign);
            Long startTime = DateUtils.getToadyBeginTime();
            Long endTime = DateUtils.getTodayEndTime();
            if (time > startTime && time < endTime) {
                QAILog.d(TAG, "Type " + key + " star:" + star);
                return star;
            }
        }
        QAILog.d(TAG, "checkStatus sign empty " +key);
        return -1;
    }


    private static void setStatus(Context context, String key, int star) {
        Long time = System.currentTimeMillis();
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME_UI,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String already_post = "{\"time\":\"" + time + "\", \"star\":\"" + star + "\"}";
        editor.putString(key, already_post);
        editor.commit();
    }

    private static void setStatus(Context context, String key, int star, Long time) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME_UI,
                Context.MODE_PRIVATE);
        QAILog.d(TAG, "setStatus key" +key +" time " +time);
        SharedPreferences.Editor editor = sp.edit();
        String already_post = "{\"time\":\"" + time + "\", \"star\":\"" + star + "\"}";
        editor.putString(key, already_post);
        editor.commit();
    }

    public static void showTaskDialog(Context context, int gotCredit, boolean b) {
        Intent i = new Intent();
        i.setClassName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.activity.DialogTaskActivity");
        i.putExtra("credit_got", gotCredit);//学分
        i.putExtra("is_success", b);//领取成功
        context.startActivity(i);
    }

    public static void showBaikeDialog(Context context, int gotCredit) {
        Intent i = new Intent();
        i.setClassName("com.kinstalk.her.qchat", "com.kinstalk.her.qchat.activity.DialogBaikeActivity");
        i.putExtra("credit_got", gotCredit);//学分
        context.startActivity(i);
    }

    public static String getUpdateTime(Context context) {
        return getUpdateTime(context, updateTimeKey);
    }

    private static String getUpdateTime(Context context, String key) {
        String time = "";
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME_TIME,
                Context.MODE_PRIVATE);
        time = sp.getString(key, "");
        return time;
    }

    public static void setUpdateTime(Context context, String time) {
        setUpdateTime(context, updateTimeKey, time);
    }

    private static void setUpdateTime(Context context, String key, String time) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME_TIME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, time);
        editor.commit();
    }

}
