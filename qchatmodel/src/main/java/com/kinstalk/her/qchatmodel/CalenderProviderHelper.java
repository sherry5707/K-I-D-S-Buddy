package com.kinstalk.her.qchatmodel;

/**
 * Created by Tracy on 2018/4/21.
 * Partially from Reminder APP
 */
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.constant.RemindConstant;
import com.kinstalk.her.qchatmodel.entity.CalendarEvent;
import com.kinstalk.her.qchatmodel.entity.RemindEntity;
import com.kinstalk.her.qchatmodel.recurrence.RecurrenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static android.provider.CalendarContract.ACCOUNT_TYPE_LOCAL;

public class CalenderProviderHelper {
        private static final String TAG = "CalenderProviderHelper";
        private static final Uri calendarsUri = CalendarContract.Calendars.CONTENT_URI;
        private static final Uri eventsUri = CalendarContract.Events.CONTENT_URI;
        private static final Uri remindersUri = CalendarContract.Reminders.CONTENT_URI;
        private static final Uri instancesUri = CalendarContract.Instances.CONTENT_URI;
        public static final String ACCOUNT_SLEEP = "kinstalk.sleep";
        public static final String ACCOUNT_GETUP = "kinstalk.getup";
        public static final String ACCOUNT_WORK = "kinstalk.work";
        public static final String ACCOUNT_NORMAL = "kinstalk.qchat";
        public static final String ACCOUNT_SELFHABIT = "kinstalk.selfhabit";
        public static final String ACCOUNT_XIAOWEI = "kinstalk_allapp";

        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_XIAOWEI = 2;
        public static final int TYPE_GETUP = 3;
        public static final int TYPE_SLEEP = 4;
        public static final int  TYPE_WORK = 5;
        public static final int TYPE_SELFHABIT = 6;
        public static final int TYPE_INTERVAL = 20000;

        static final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.RRULE
        };

        static final String[] REMINDER_PROJECTION = new String[]{
                CalendarContract.Instances.BEGIN
        };

    @SuppressLint("MissingPermission")
        public static boolean insertEventAndReminder(Context context, String title, long beginTime, String rrule, String id, int type) {
            if(TYPE_GETUP == type) {
               return insertEventAndReminder(context,title,beginTime,rrule,id,ACCOUNT_GETUP);
            } else  if(TYPE_SLEEP ==type) {
                return insertEventAndReminder(context,title,beginTime,rrule,id,ACCOUNT_SLEEP);
            } else if( TYPE_WORK == type) {
                return insertEventAndReminder(context, title, beginTime, rrule, id, ACCOUNT_WORK);
            } else if (TYPE_NORMAL == type) {
                return insertEventAndReminder(context, title, beginTime, rrule, id, ACCOUNT_NORMAL);
            } else if(type > TYPE_WORK ){
                return insertEventAndReminder(context, title, beginTime, rrule, id, ACCOUNT_SELFHABIT);
            } else
                return false;
        }

    @SuppressLint("MissingPermission")
    public static boolean insertEventAndReminder(Context context, String title, long beginTime, String duration,String rrule, String id, int type) {
        if(TYPE_GETUP == type) {
            return insertEventAndReminder(context,title,beginTime,duration,rrule,id,ACCOUNT_GETUP);
        } else  if(TYPE_SLEEP ==type) {
            return insertEventAndReminder(context,title,beginTime,duration,rrule,id,ACCOUNT_SLEEP);
        } else if( TYPE_WORK == type) {
            return insertEventAndReminder(context, title, beginTime, duration, rrule, id, ACCOUNT_WORK);
        } else if (TYPE_NORMAL == type) {
            return insertEventAndReminder(context, title, beginTime, duration, rrule, id, ACCOUNT_NORMAL);
        } else if (type > TYPE_WORK ) {
            return insertEventAndReminder(context, title, beginTime, duration, rrule, id, ACCOUNT_SELFHABIT);
        } else
            return false;
    }

    @SuppressLint("MissingPermission")
    public static boolean insertEventAndReminder(Context context, String title, long beginTime, String rrule, String id, String AccountName) {
        // 获取日历账户的id
        if(TextUtils.isEmpty(AccountName))
            return false;
        int intID = Integer.parseInt(id);
        if(AccountName.equals(ACCOUNT_SLEEP))
            intID +=200000;
        else if(AccountName.equals(ACCOUNT_GETUP))
            intID += 300000;
        else if(AccountName.equals(ACCOUNT_WORK))
            intID += 400000;
        else if(AccountName.equals(ACCOUNT_SELFHABIT))
            intID += 500000;
        else if(AccountName.equals(ACCOUNT_NORMAL))
            intID += 100000;
        else
            return false;
        int calId = getCalendarAccount(context, AccountName);
        if (calId < 0) {
            // 获取账户id失败直接返回，添加日历事件失败
            return false;
        }
        try {
            String eventID = String.valueOf(intID);
            //添加事件
            ContentValues event = new ContentValues();
            event.put(CalendarContract.Events._ID, eventID);
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.CALENDAR_ID, calId);
            event.put(CalendarContract.Events.DTSTART, beginTime);
            event.put(CalendarContract.Events.DTEND, beginTime + 60 * 1000);
            event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
            event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            if (!TextUtils.isEmpty(rrule)) {
                event.put(CalendarContract.Events.RRULE, rrule);
            }
            Uri newEvent = context.getContentResolver().insert(eventsUri, event);
            if (newEvent == null) {
                return false;
            }

            //添加提醒
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
            values.put(CalendarContract.Reminders.MINUTES, 0);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri uri = context.getContentResolver().insert(remindersUri, values);
            if (uri == null) {
                return false;
            }
            if(AccountName.equals(ACCOUNT_SELFHABIT)) {
                ContentValues preValues = new ContentValues();
                preValues.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
          //      if (beginTime - System.currentTimeMillis() > 10 * 60 * 1000) {
                    preValues.put(CalendarContract.Reminders.MINUTES, 10 );
                    preValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                    context.getContentResolver().insert(remindersUri, preValues);
       //         }
            }
            if(AccountName.equals(ACCOUNT_SELFHABIT)) {
                ContentValues preValues = new ContentValues();
                preValues.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
     //           if (beginTime - System.currentTimeMillis() > 10 * 60 * 1000) {
                    preValues.put(CalendarContract.Reminders.MINUTES, -30 );
                    preValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                    context.getContentResolver().insert(remindersUri, preValues);
    //            }
            }
        }catch (Exception e) {
            QAILog.d(TAG, e);
            return false;
        }
        return true;
    }

        /**
         * 获取提醒事件
         *
         * @param context
         * @return
         */
        @SuppressLint("MissingPermission")
        public static List<CalendarEvent> getEvents(Context context) {
            //构建Instance表查询范围
            long accountId = getLocalReminderCalendarAccount(context);
            if(accountId == -1)
                return null;
            Calendar startTime = Calendar.getInstance();
            startTime.add(Calendar.DAY_OF_MONTH, 0);
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 0);
            long startMillis = startTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(startMillis);
            endTime.add(Calendar.YEAR, 1);
            long endMillis = endTime.getTimeInMillis();
            QAILog.d(TAG,"QueryEvent: start->" + getFormatTime(startMillis) + "  end->" + getFormatTime(endMillis));
            Uri.Builder builder = instancesUri.buildUpon();
            ContentUris.appendId(builder, startMillis);
            ContentUris.appendId(builder, endMillis);
            //查询Event表

            String event_selection = CalendarContract.Events.CALENDAR_ID + "=? AND (" + CalendarContract.Events.DTSTART + ">=? OR "
                    + CalendarContract.Events.RRULE + " is not NULL" + ")";
            String[] event_selection_args = new String[]{String.valueOf(accountId),
                    String.valueOf(startMillis)
            };
            String eventSort = CalendarContract.Events.DTSTART + " ASC";
            Cursor eventsCursor = context.getContentResolver().query(
                    eventsUri,
                    EVENT_PROJECTION,
                    event_selection,
                    event_selection_args,
                    eventSort);
            List<CalendarEvent> dataList = new ArrayList<>();
            long startDate = 0L;
            try {
                if (eventsCursor != null) {
                    while (eventsCursor.moveToNext()) {
                        CalendarEvent event = new CalendarEvent();
                        RemindEntity remind = new RemindEntity();

                        String eid = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events._ID));
                        String calendarId = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
                        String title = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events.TITLE));
                        String rrule = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events.RRULE));
                        if (!TextUtils.isEmpty(rrule)) {
                            String reminder_selection = CalendarContract.Instances.EVENT_ID + " =? AND " + CalendarContract.Instances.BEGIN + " >=?";
                            String[] reminder_selection_args = new String[]{eid, String.valueOf(startMillis)};
                            String reminderSort = CalendarContract.Instances.BEGIN + " ASC";
                            Cursor reminderCursor = context.getContentResolver().query(
                                    builder.build(),
                                    REMINDER_PROJECTION,
                                    reminder_selection,
                                    reminder_selection_args,
                                    reminderSort);
                            try {
                                if (reminderCursor.moveToFirst()) {
                                    startDate = reminderCursor.getLong(reminderCursor.getColumnIndex(CalendarContract.Instances.BEGIN));
                                    event.setStartTime(startDate);
                                } else {
                                    startDate = eventsCursor.getLong(eventsCursor.getColumnIndex(CalendarContract.Events.DTSTART));
                                    event.setStartTime(startDate);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (reminderCursor != null) {
                                    reminderCursor.close();
                                }
                            }
                            String ruleFormat = RecurrenceHelper.getNumericRruleValue(context, rrule);
                            QAILog.d(TAG, "QueryEvent.rrule:" + rrule + "      " + ruleFormat);
                            event.setRruleFormat(ruleFormat);
                        } else {
                            startDate = eventsCursor.getLong(eventsCursor.getColumnIndex(CalendarContract.Events.DTSTART));
                            event.setStartTime(startDate);
                        }
                        event.setCalendarId(calendarId);
                        event.setEventId(eid);
                        event.setTitle(title);
                        event.setRrule(rrule);
                  //      event.setType(getRemindType(context, eid));
                        QAILog.d(TAG, "QueryEvent.dataFormat:" + "title->" + event.getTitle() + "      dtStart->" + getFormatTime(event.getStartTime()));
                        dataList.add(event);
                    }
                }
            } finally {
                if (eventsCursor != null) {
                    eventsCursor.close();
                }
            }
            return dataList;
        }

    @SuppressLint("MissingPermission")
    public static boolean insertEventAndReminder(Context context, String title, long beginTime, String duration, String rrule, String id, String AccountName) {
        // 获取日历账户的id
        if(TextUtils.isEmpty(AccountName))
            return false;
        int intID = Integer.parseInt(id);
        if(AccountName.equals(ACCOUNT_SLEEP))
            intID +=200000;
        else if(AccountName.equals(ACCOUNT_GETUP))
            intID += 300000;
        else if(AccountName.equals(ACCOUNT_WORK))
            intID += 400000;
        else if(AccountName.equals(ACCOUNT_SELFHABIT))
            intID += 500000;
        else if(AccountName.equals(ACCOUNT_NORMAL))
            intID += 100000;
        else
            return false;
        int calId = getCalendarAccount(context, AccountName);
        if (calId < 0) {
            // 获取账户id失败直接返回，添加日历事件失败
            return false;
        }
        try {
            String eventID = String.valueOf(intID);
            //添加事件
            ContentValues event = new ContentValues();
            event.put(CalendarContract.Events._ID, eventID);
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.CALENDAR_ID, calId);

            event.put(CalendarContract.Events.DTSTART, beginTime);
            event.put(CalendarContract.Events.DTEND, beginTime + 60 * 1000);
            event.put(CalendarContract.Events.DURATION, duration);
            event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
            event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            if (!TextUtils.isEmpty(rrule)) {
                event.put(CalendarContract.Events.RRULE, rrule);
            }
            Uri newEvent = context.getContentResolver().insert(eventsUri, event);
            if (newEvent == null) {
                return false;
            }

            //添加提醒
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
            values.put(CalendarContract.Reminders.MINUTES, 0);
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri uri = context.getContentResolver().insert(remindersUri, values);
            if (uri == null) {
                return false;
            }
            ContentValues preValues = new ContentValues();
            preValues.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
            if (beginTime - System.currentTimeMillis() > 24 * 60 * 60 * 1000) {
                preValues.put(CalendarContract.Reminders.MINUTES, 24 * 60);
            } else {
                preValues.put(CalendarContract.Reminders.MINUTES, (int) Math.ceil((double) (beginTime - System.currentTimeMillis()) / (double) (60 * 1000)));
            }
            preValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            context.getContentResolver().insert(remindersUri, preValues);
        }catch (Exception e) {
            QAILog.d(TAG, e);
            return false;
        }
        return true;
    }

    /**
     * 获取提醒事件
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static List<RemindEntity> getReminderEvents(Context context) {
        //构建Instance表查询范围
        long accountId = getLocalReminderCalendarAccount(context);
        if(accountId == -1)
            return null;
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.DAY_OF_MONTH, 0);
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        long startMillis = startTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(startMillis);
        endTime.add(Calendar.YEAR, 1);
        long endMillis = endTime.getTimeInMillis();
        QAILog.d(TAG,"QueryEvent: start->" + getFormatTime(startMillis) + "  end->" + getFormatTime(endMillis));
        Uri.Builder builder = instancesUri.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);
        //查询Event表

        String event_selection = CalendarContract.Events.CALENDAR_ID + "=? AND (" + CalendarContract.Events.DTSTART + ">=? OR "
                + CalendarContract.Events.RRULE + " is not NULL" + ")";
        String[] event_selection_args = new String[]{String.valueOf(accountId),
                String.valueOf(startMillis)
        };
        String eventSort = CalendarContract.Events.DTSTART + " ASC";
        Cursor eventsCursor = context.getContentResolver().query(
                eventsUri,
                EVENT_PROJECTION,
                event_selection,
                event_selection_args,
                eventSort);
        List<RemindEntity> remindList = new ArrayList<>();
        long startDate = 0L;
        String ruleFormat = "";
        try {
            if (eventsCursor != null) {
                while (eventsCursor.moveToNext()) {
                    RemindEntity remind = new RemindEntity();

                    String eid = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events._ID));
                    String calendarId = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
                    String title = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events.TITLE));
                    String rrule = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events.RRULE));
                    ruleFormat = "";
                    startDate = 0L;
                    if (!TextUtils.isEmpty(rrule)) {
                        String reminder_selection = CalendarContract.Instances.EVENT_ID + " =? AND " + CalendarContract.Instances.BEGIN + " >=?";
                        String[] reminder_selection_args = new String[]{eid, String.valueOf(startMillis)};
                        String reminderSort = CalendarContract.Instances.BEGIN + " ASC";
                        Cursor reminderCursor = context.getContentResolver().query(
                                builder.build(),
                                REMINDER_PROJECTION,
                                reminder_selection,
                                reminder_selection_args,
                                reminderSort);
                        try {
                            if (reminderCursor.moveToFirst()) {
                                QAILog.d(TAG, "reminder cursor");
                                startDate = reminderCursor.getLong(reminderCursor.getColumnIndex(CalendarContract.Instances.BEGIN));
                            } else {
                                startDate = eventsCursor.getLong(eventsCursor.getColumnIndex(CalendarContract.Events.DTSTART));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (reminderCursor != null) {
                                reminderCursor.close();
                            }
                        }
                        ruleFormat = RecurrenceHelper.getNumericRruleValue(context, rrule);
                        QAILog.d(TAG, "QueryEvent.rrule:" + rrule + "      " + ruleFormat);
                    } else {
                        startDate = eventsCursor.getLong(eventsCursor.getColumnIndex(CalendarContract.Events.DTSTART));
                    }
                    //      event.setType(getRemindType(context, eid));
                    QAILog.d(TAG, "QueryEvent.dataFormat:" + "title->" + title + "      dtStart->" + getFormatTime(startDate));

                    int weekDay = DateUtils.getWeekDay();
                    String day = Integer.toString(weekDay);
                    Long beginMills = DateUtils.getToadyBeginTime();
                    Long endMills = DateUtils.getTodayEndTime();
                    QAILog.d(TAG, "QueryEvent.beginMills:" + beginMills + "      endMills->" + endMills +"      startDate->" + startDate);
                    if((!TextUtils.isEmpty(ruleFormat) && ruleFormat.contains(day)) ||
                            ((beginMills < startDate) && (endMills > startDate))) {
                        remind.setOpenid(null);
                        remind.setRemindid(eid);
                        remind.setContent(title);
                        remind.setAssign_time(0L);
                        remind.setRemind_time(startDate);
                        remind.setRepeat_time(rrule);
                        remind.setStatus(0);
                        remind.setType(2);
                        remindList.add(remind);
                    }
                }
            }
        } finally {
            if (eventsCursor != null) {
                eventsCursor.close();
            }
        }
        return remindList;
    }

        @SuppressLint("MissingPermission")
        private static int getRemindType(Context context, String eventId) {
            int remindType = RemindConstant.RemindType.Type_Normal;
            String reminder_selection = CalendarContract.Reminders.EVENT_ID + " =?";
            String[] reminder_selection_args = new String[]{eventId};
            Cursor reminderCursor = context.getContentResolver().query(
                    remindersUri,
                    null,
                    reminder_selection,
                    reminder_selection_args,
                    null);
            try {
                if (reminderCursor.moveToFirst()) {
                    remindType = reminderCursor.getInt(reminderCursor.getColumnIndex("type"));
                }
            } finally {
                if (reminderCursor != null) {
                    reminderCursor.close();
                }
            }
            return remindType;
        }

        public static String getFormatTime(long time) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(time);
            return dateStr;
        }

        /**
         * 删除提醒
         *
         * @param context
         * @param eventId
         */
        @SuppressLint("MissingPermission")
        public static void deleteEvent(Context context, String eventId, int type) {
            int id = Integer.parseInt(eventId);
            if(TYPE_NORMAL == type)
                id += 100000;
            else if(TYPE_SELFHABIT == type) {
                id += 500000;
            }
            String eventID = String.valueOf(id);
            try {
                String selection = CalendarContract.Events._ID + " =" + eventID;
                context.getContentResolver().delete(
                        eventsUri,
                        selection,
                        null);

                String reminderSelection = CalendarContract.Reminders.EVENT_ID + " =" + eventId;
                context.getContentResolver().delete(remindersUri, reminderSelection, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public static void deleteAllEvents(Context context, int type) {
            if(TYPE_NORMAL == type) {
                deleteAllEvents(context, ACCOUNT_NORMAL);
            } else if(TYPE_GETUP == type) {
                deleteAllEvents(context, ACCOUNT_GETUP);
            } else if(TYPE_SLEEP == type) {
                deleteAllEvents(context, ACCOUNT_SLEEP);
            } else if(TYPE_WORK == type) {
                deleteAllEvents(context, ACCOUNT_WORK);
            } else if(TYPE_SELFHABIT == type) {
                deleteAllEvents(context, ACCOUNT_SELFHABIT);
            }
        }

        @SuppressLint("MissingPermission")
        public static void deleteAllEvents(Context context, String accountName) {
           String selection_account = CalendarContract.Events.CALENDAR_ID + " =" +getCalendarAccount(context, accountName);
            try {
                Cursor eventsCursor = context.getContentResolver().query(
                        eventsUri,
                        EVENT_PROJECTION,
                        selection_account,
                        null,
                        null);
                if (eventsCursor != null) {
                    while (eventsCursor.moveToNext()) {
                        String eventId = eventsCursor.getString(eventsCursor.getColumnIndex(CalendarContract.Events._ID));
                        String selection = CalendarContract.Events._ID + " =" + eventId;

                        context.getContentResolver().delete(
                                eventsUri,
                                selection,
                                null);

                        String reminderSelection = CalendarContract.Reminders.EVENT_ID + " =" + eventId;
                        context.getContentResolver().delete(remindersUri, reminderSelection, null);
                    }
                    if (ACCOUNT_SLEEP.equals(accountName)) {
                        context.sendBroadcast(new Intent("sleep_finish"));
                    }
                    if (ACCOUNT_GETUP.equals(accountName)) {
                        context.sendBroadcast(new Intent("wakeup_finish"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public static void deleteAllEvents(Context context) {
            deleteAllEvents(context, ACCOUNT_NORMAL);
            deleteAllEvents(context, ACCOUNT_GETUP);
            deleteAllEvents(context, ACCOUNT_SLEEP);
            deleteAllEvents(context, ACCOUNT_SELFHABIT);
        }
        /**
         * 获取日历账户
         *
         * @param context
         * @return
         */
        @SuppressLint("MissingPermission")
        private static int getCalendarAccount(Context context, String accountName) {
            QAILog.d(TAG, "getCalendarAccount");
            String selection = CalendarContract.Calendars.ACCOUNT_NAME + "=?";
            String[] selectionArgs = new String[]{accountName};

            Cursor userCursor = context.getContentResolver().query(calendarsUri, null, selection, selectionArgs, null);
            try {
                if (userCursor != null && userCursor.moveToFirst()) {
                    return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
                }

                return insertCalendarAccount(context, accountName);
            } finally {
                if (userCursor != null) {
                    userCursor.close();
                }
            }
        }

    /**
     * 获取日历账户
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    private static int getLocalReminderCalendarAccount(Context context) {
        QAILog.d(TAG, "getLocalReminderCalendarAccount");
        String selection = CalendarContract.Calendars.ACCOUNT_NAME + "=?";
        String[] selectionArgs = new String[]{"kinstalk_allapp"};
        QAILog.d(TAG, "calendarUri");
        Cursor userCursor = context.getContentResolver().query(calendarsUri, null, selection, selectionArgs, null);
        try {
            if (userCursor != null && userCursor.moveToFirst()) {
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            }

            return -1;
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private static int insertCalendarAccount(Context context, String accountName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        contentValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL);
        contentValues.put(CalendarContract.Calendars.NAME, accountName);
        contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, accountName);
        contentValues.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        contentValues.put(CalendarContract.Calendars.VISIBLE, 1);
        contentValues.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        contentValues.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());
        contentValues.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        contentValues.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri insertUri = calendarsUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL)
                .build();

        Uri result = context.getContentResolver().insert(insertUri, contentValues);
        long id = result == null ? -1 : ContentUris.parseId(result);

        return (int) id;
    }

    /**
     * 根据给定的ID，判断是否是当前账号对应的
     *
     * @param eventId
     * @return
     */
    public static boolean bAccountForKids(Context context, long eventId) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        // 获取日历账户的id
        int calId = getCalendarAccount(context, ACCOUNT_NORMAL);
        if (calId < 0) {
            // 获取账户id失败直接返回
            return false;
        }

        String selection = CalendarContract.Events._ID + "=?";
        String[] args = new String[] {String.valueOf(eventId)};

        Cursor cursor = context.getContentResolver().query(
                eventsUri,
                null,
                selection,
                args,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            long accountId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
            if (accountId == calId) {
                return true;
            }
        }

        return false;
    }

    /**
     * 根据给定的ID，判断是否是当前账号对应的
     *
     * @param eventId
     * @return
     */
    public static int getAccountType(Context context, long eventId) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return -1;
        }

        // 获取日历账户的id
        int kidsID = getCalendarAccount(context, ACCOUNT_NORMAL);
        int sleepID = getCalendarAccount(context, ACCOUNT_SLEEP);
        int getupID = getCalendarAccount(context, ACCOUNT_GETUP);
        int selfHabitId = getCalendarAccount(context, ACCOUNT_SELFHABIT);
        int allappID = getCalendarAccount(context, ACCOUNT_XIAOWEI);

        String selection = CalendarContract.Events._ID + "=?";
        String[] args = new String[] {String.valueOf(eventId)};

        Cursor cursor = context.getContentResolver().query(
                eventsUri,
                null,
                selection,
                args,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            long accountId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
            if (accountId != -1 && accountId == kidsID) {
                return TYPE_NORMAL;
            } else if (accountId != -1 && accountId == sleepID) {
                return TYPE_SLEEP;
            } else if(accountId != -1 && accountId == getupID) {
                return TYPE_GETUP;
            }  else if(accountId != -1 && accountId == selfHabitId) {
                return TYPE_SELFHABIT;
            } else if(accountId != -1 && accountId == allappID) {
                return TYPE_XIAOWEI;
            }
        }
        return -1;
    }
}
