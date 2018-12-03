package com.kinstalk.her.qchat.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.os.PowerManager;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;
import com.kinstalk.her.qchat.voice.RingPlayer;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.kinstalk.util.AppUtils;

/**
 * Created by Tracy on 2018/4/19.
 */

public class UIHelper {
    private static String TAG = "UIHelper";
    public static String SHARED_PREFERENCE_NAME_UI = "com.kinstalk.her.qchat.update_preferences_ui";
    public static String SHARED_PREFERENCE_NAME_GUIDE = "com.kinstalk.her.qchat.homework.guide";
    public static String SHARED_PREFRENCE_CLEAR_XG = "com.kinstalk.her.qchat.clear.XG";
    public static final String ACTION_CLEAR_HOME = "com.kinstalk.her.qchat.clearhome";
    public static final String QRCODE_CHECK = "com.kinstalk.her.qchat.QRCode";
    public static final String SWITCH_WX = "com.kinstalk.her.qchat.switch.wx";

    public static String msgID = "";
    public static Long timeStamp = 0L;
    private static final String sleepTime = "SLEEP_TIME";
    private static final String getupTime = "GETUP_TIME";
    private static final String homeGuide = "home_guide";

    public static String DateFormat(long time) {
        Date date = new Date(time);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return f.format(date);
    }

    public static boolean isTopActivity2(String pkgName) {
        try {
            boolean is = AppUtils.isTopAcitivity(pkgName);
            Log.e(TAG, "isTopActivity " + pkgName + " " + is);
            return is;
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
        return false;
    }

    public static void wakeupAndDong(final Context ctx) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                wakeUp(ctx);
                if (!QchatApplication.isWakeupActive() || !QchatApplication.isSleepingActive()) {
                    //非起床睡觉播放故事
                    RingPlayer.getInstance().startRing();
                }
            }
        }).run();
    }

    public static void Dong(final Context ctx) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!QchatApplication.isWakeupActive() || !QchatApplication.isSleepingActive()) {
                    //非起床睡觉播放故事
                    RingPlayer.getInstance().startRing();
                }
            }
        }).run();
    }

    public static void wakeUp(final Context ctx) {
        try {
            AppUtils.wakeupDevice(ctx, SystemClock.uptimeMillis());
        } catch (Exception e) {
        }
    }


    //  判断起床有无提前打卡
    public static boolean checkGetupOrNot(Context context) {
        return checkStatus(context, getupTime);
    }

    public static void setAlreadyGetup(Context context) {
        setStatus(context, sleepTime);
    }

    //判断睡觉有无提前打卡
    public static boolean checkSleepOrNot(Context context) {
        return checkStatus(context, sleepTime);
    }

    public static void setAlreadySleep(Context context) {
        setStatus(context, sleepTime);
    }

    private static boolean checkStatus(Context context, String key) {
        Long time = 0L;
        SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFERENCE_NAME_UI,
                Context.MODE_PRIVATE);
        time = sp.getLong(key, 0L);
        Long startTime = DateUtils.getToadyBeginTime();
        Long endTime = DateUtils.getTodayEndTime();
        if (time > startTime && time < endTime) {
            return true;
        }
        return false;
    }

    private static void setStatus(Context context, String key) {
        Long time = SystemClock.currentThreadTimeMillis();
        SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFERENCE_NAME_UI,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, time);
        editor.commit();
    }

    public static void clearAllSharedPreferences() {
        clearSharedPreferences(SHARED_PREFERENCE_NAME_UI);
        clearSharedPreferences("work_time");
        clearSharedPreferences("work_finish");
    }

    public static void clearAndReboot(Context context) {
        QAILog.d(TAG, "Begin");
        setPropertyForXGClear(context);
        clearAllMIDSharedPreferences(context);
        QAILog.d(TAG, "end");
        rebootSystem(context);
    }

    private static void clearAllMIDSharedPreferences(Context context) {
        clearSharedPreferences("com.kinstalk.her.qchat.mid.world.ro");
        clearSharedPreferences("com.kinstalk.her.qchat.push_sync");
        clearSharedPreferences("com.kinstalk.her.qchat.self_push_sync");
        clearSharedPreferences("tpush.shareprefs");
        clearSharedPreferences("com.kinstalk.her.qchat_preferences");
        Settings.System.putString(context.getContentResolver(),"__MTA_DEVICE_INFO__3","");
        Settings.System.putString(context.getContentResolver(), "__MTA_DEVICE_INFO__1000001", "");
        Settings.System.putString(context.getContentResolver(), "__MTA_DEVICE_INFO_CHECK_ENTITY__3", "");
    }

    public static void ClearPropertyForXGClear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFRENCE_CLEAR_XG,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("clear");
        editor.commit();
    }

    public static void setPropertyForXGClear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFRENCE_CLEAR_XG,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("clear", 1);
        editor.commit();
    }

    public static void ClearPropertyForXGActivate(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFRENCE_CLEAR_XG,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("activate");
            editor.commit();
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
    }

    public static void setPropertyForXGActivate(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFRENCE_CLEAR_XG,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("activate", 1);
            editor.commit();
            } catch (Exception e) {
            QAILog.d(TAG, e);
        }
    }

    public static int getPropertyForXGActivate(Context context) {
        int status = 0;
        SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFRENCE_CLEAR_XG,
                Context.MODE_PRIVATE);
        status = sp.getInt("activate", 0);
        return status;
    }

    public static int getPropertyForXGClear(Context context) {
        int status = 0;
        SharedPreferences sp = context.getSharedPreferences(UIHelper.SHARED_PREFRENCE_CLEAR_XG,
                Context.MODE_PRIVATE);
        status = sp.getInt("clear", 0);
        return status;
    }

    public static void setHomeGuideAlreadyShow(Context context) {
        setKeyStatusToSP(context, homeGuide);
    }

    public static boolean getHomeGuideAlreadyShow(Context context) {
        return spKeyStatus(context, homeGuide);
    }

    private static boolean spKeyStatus(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCE_NAME_GUIDE, Context.MODE_PRIVATE);
        boolean status = sp.getBoolean(key, false);
        return status;
    }

    private static void setKeyStatusToSP(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCE_NAME_GUIDE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, true);
        editor.commit();
        Log.d(TAG, "setKey");
    }

    private static void rebootSystem(Context context) {
        PowerManager pManager=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pManager.reboot(null);
    }

    public static void clearSignSharedPrefrences( Context context, int type) {
        UIHelp.setAlreadySign(context, type, -1);
    }

    public static void setWorkTimeSharedPreferences() {
        //首次绑定，没有设置work_time，引发问题：getHomeworkMessage时间参数会取不到，不能get到本地的dataList
        SharedPreferences preferences = BaseApplication.getInstance().getSharedPreferences("work_time", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("work_alert_time", System.currentTimeMillis()).commit();
    }

    public static void clearSharedPreferences(String name) {
        SharedPreferences preferences = BaseApplication.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }

    public static void clearSharedPreferences(String name, String key) {
        SharedPreferences preferences = BaseApplication.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void  checkHomeworkAnimation(final Context context) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                if (SharedPreferencesUtils.containsSharedPreferences(context, SharedPreferencesUtils.WORK_TIME, SharedPreferencesUtils.WORK_ALERT_TIME)) {
                    long spWorkTime = SharedPreferencesUtils.getSharedPreferencesWithLong(context, SharedPreferencesUtils.WORK_TIME, SharedPreferencesUtils.WORK_ALERT_TIME, DateUtils.getToadyBeginTime());
                    if (spWorkTime > 0) {
                        List<QHomeworkMessage> messageList = MessageManager.getHomeworkMessage(spWorkTime);
                        if (messageList == null || messageList.size() == 0)
                            UIHelp.sendEnterHomeworkBroadcast(context);
                    } else {
                        List<QHomeworkMessage> messageList = MessageManager.getHomeworkMessage(DateUtils.getToadyBeginTime());
                        if (messageList == null || messageList.size() == 0)
                            UIHelp.sendEnterHomeworkBroadcast(context);
                    }
                }
            }
        });
    }

    public static boolean xiaoweiBindStatus(Context context) {
        return (1 == Settings.Secure.getInt(context.getContentResolver(), "xiaowei_bind_status", 0));
    }

    public static boolean wchatBindStatus(Context context) {
        return (1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0));
    }

    public static String getMsgID() {
        return msgID;
    }

    public static void setMsgID(String id) {
        msgID = id;
    }

    public static long getTimestamp() {
        return timeStamp;
    }

    public static void setTimestamp(Long stamp) {
        timeStamp = stamp;
    }

}
