package com.kinstalk.her.qchat.remind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.kinstalk.her.qchat.AlarmActivity;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.RemindCustomActivity;
import com.kinstalk.her.qchat.activity.SleepActivity;
import com.kinstalk.her.qchat.activity.WakeUpActivity;
import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.voice.QRecorderManager;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.CalenderProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;

/**
 * Created by Tracy on 2018/4/21.
 * partially introduce from reminder app
 */

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "QReminderReceiver";
    private static final int SHOW_REMINDER = 1;
    static final String[] ALERT_PROJECTION = new String[]{
            CalendarContract.CalendarAlerts._ID,
            CalendarContract.CalendarAlerts.EVENT_ID,
            CalendarContract.CalendarAlerts.TITLE,
            CalendarContract.CalendarAlerts.DTSTART,
            CalendarContract.CalendarAlerts.MINUTES,
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_REMINDER:
                    Object[] mObj = (Object[]) msg.obj;
                    Context context = (Context) mObj[0];
                    Intent intent = (Intent) mObj[1];
                    QAILog.d(TAG, "Add Message");
                    showReminder(context, intent);
                    break;
                default:
                    break;

            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(final Context context, final Intent intent) {
        QAILog.d("ReminderReceiver action->" + intent.getAction());
        if (CalendarContract.ACTION_EVENT_REMINDER.equals(intent.getAction())) {
            QAILog.d(TAG, "showReminder");
            showReminder(context, intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showReminder(final Context context, final Intent intent) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Uri uri = intent.getData();
                String alertTime = uri.getLastPathSegment();
                String xwReminderTitle = "";
                boolean haveXWReminder = false;
                String selection = CalendarContract.CalendarAlerts.ALARM_TIME + "=?";
                Cursor cursor = context.getContentResolver().query(CalendarContract.CalendarAlerts.CONTENT_URI_BY_INSTANCE, ALERT_PROJECTION, selection, new String[]{alertTime}, null);
                try {
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        int id = cursor.getInt(cursor.getColumnIndex(CalendarContract.CalendarAlerts.EVENT_ID));
                        int type = CalenderProviderHelper.getAccountType(context, id);
                        if (type == CalenderProviderHelper.TYPE_XIAOWEI) {
                            MessageManager.asyncNotifyReminderChange(true);
                            MessageManager.NotifyReminderToLauncher(context);
                            xwReminderTitle = cursor.getString(cursor.getColumnIndex(CalendarContract.CalendarAlerts.TITLE));
                            long minutes = cursor.getLong(cursor.getColumnIndex(CalendarContract.CalendarAlerts.MINUTES));
                            long time = cursor.getLong(cursor.getColumnIndex(CalendarContract.CalendarAlerts.DTSTART));
                            long cur = System.currentTimeMillis();
                            String begin = DateUtils.getFormatTime(time);
                            String alarmTime = DateUtils.getFormatTime(cur);
                            if ((minutes < 1) && !TextUtils.isEmpty(begin) && !TextUtils.isEmpty(alarmTime) && begin.equals(alarmTime))
                                haveXWReminder = true;
                            QAILog.d(TAG, "xiaowei Reminder title" + xwReminderTitle);
                            continue;
                        }

                        String title = cursor.getString(cursor.getColumnIndex(CalendarContract.CalendarAlerts.TITLE));
                        long time = cursor.getLong(cursor.getColumnIndex(CalendarContract.CalendarAlerts.DTSTART));
                        long minutes = cursor.getLong(cursor.getColumnIndex(CalendarContract.CalendarAlerts.MINUTES));
                        long cur = System.currentTimeMillis();
                        String begin = DateUtils.getFormatTime(time);
                        String alarmTime = DateUtils.getFormatTime(cur);
                        long beginTime = DateUtils.getSortTime(time);
                        long curTime = DateUtils.getSortTime(cur);
                        QAILog.d("ReminderReceiver showReminder:title->" + title + "   begin->" + begin + "    id->" + id + "   alarmTime->" + alarmTime + " minutes->" + minutes);
                        if (((minutes < 1) && !TextUtils.isEmpty(begin) && !TextUtils.isEmpty(alarmTime) && begin.equals(alarmTime))
                                || (minutes == 10 && ((beginTime - curTime == 10) || (beginTime - curTime == 50 && (beginTime / 100 > curTime / 100))))
                                || (minutes == -30 && ((curTime - beginTime == 30) || (curTime - beginTime == 70 && (curTime / 100 > beginTime / 100))))) {
                            if (UIHelper.xiaoweiBindStatus(context)) {
                                if (CalenderProviderHelper.TYPE_NORMAL == type && !QchatApplication.isSleepingActive() && !QchatApplication.isWakeupActive() &&
                                        (minutes < 1) && !TextUtils.isEmpty(begin) && !TextUtils.isEmpty(alarmTime) && begin.equals(alarmTime)) {
                                    AlarmActivity.actionStart(context, time, title);
                                } else if (CalenderProviderHelper.TYPE_SLEEP == type &&
                                        (minutes < 1) && !TextUtils.isEmpty(begin) && !TextUtils.isEmpty(alarmTime) && begin.equals(alarmTime)) {
                                    QAILog.d(TAG, "Sleep Activity");
                                    if (!UIHelper.checkSleepOrNot(context) && (UIHelp.checkSleepSignOrNot(context) < 0)) {
                                        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
                                        int star = MessageManager.getHabitStar(context, type);
                                        SleepActivity.actionStart(context, time, (kidsEntity != null) ? kidsEntity.nick_name : "宝宝", star);
                                    }
                                } else if (CalenderProviderHelper.TYPE_GETUP == type && (minutes < 1) && !TextUtils.isEmpty(begin) && !TextUtils.isEmpty(alarmTime) && begin.equals(alarmTime)) {
                                    QAILog.d(TAG, "Getup Activity");
                                    if (!UIHelper.checkGetupOrNot(context) && (UIHelp.checkGetupSignOrNot(context) < 0)) {
                                        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
                                        int star = MessageManager.getHabitStar(context, type);
                                        WakeUpActivity.actionStart(context, time, (kidsEntity != null) ? kidsEntity.nick_name : "宝宝", star);
                                    }
                                } else if (CalenderProviderHelper.TYPE_SELFHABIT == type) {
                                    int habitType = MessageManager.getHabitType(time);
                                    if(type == -1) {
                                        QAILog.d(TAG, "qchat db is null");
                                        break;
                                    }
                                    QAILog.d(TAG, "habitType " + habitType + "Time " + time);
                                    if (UIHelp.checkHabitSignOrNot(context, habitType) < 0) {
                                        //启动自定义习惯打卡activity
                                        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
                                        int star = MessageManager.getHabitStar(context, habitType);
                                        int status = 1;
                                        if (minutes == 0) {
                                            status = 0;
                                        } else if (minutes == 10) {
                                            status = 1;
                                        } else if (minutes == -30) {
                                            status = 2;
                                        }
                                        RemindCustomActivity.actionStart(context, time, (kidsEntity != null) ? kidsEntity.nick_name : "宝宝", title, star, status, habitType);
                                        if (haveXWReminder) {
                                          //  QAILog.d(TAG)
                                         //   showNotifyDialog(context, DateUtils.getRemindTime(time) + " " + xwReminderTitle);
                                        }
                                    }
                                }
                            }
                            MessageManager.NotifyReminderToLauncher(context);
                            QAILog.d(TAG, "Got Notification");
                            break;
                        }
                    }
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            }
        });
    }

    public static void showNotifyDialog(Context context, String content) {
        Intent i = new Intent();
        i.setClassName("com.kinstalk.m4", "com.kinstalk.m4.reminder.activity.RemindNotifyActivity");
        i.putExtra("content", content);//学分
        context.startActivity(i);
    }

}