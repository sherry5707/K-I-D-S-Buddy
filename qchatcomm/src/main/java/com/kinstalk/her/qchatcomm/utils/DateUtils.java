/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatcomm.utils;

import android.content.Context;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by knight.xu on 2018/4/6.
 * Modified by Tracy
 */

public class DateUtils {

    public static String getSecondsString(int seconds) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat(seconds >= 3600 ? "HH:mm:ss" : "mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }

    public static String getSecondsString2(int seconds) {
        String timeString = String.valueOf(seconds);
        return timeString;
    }

    public static String getMicroSecondsString(long microSeconds) {
        Date date = new Date(microSeconds);
        SimpleDateFormat formatter = new SimpleDateFormat(microSeconds >= 3600000 ? "HH:mm:ss" : "mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }

    public static String getAllTime(String time) {
        Date date = new Date(Long.parseLong(time));
        SimpleDateFormat sdf = new SimpleDateFormat("yy年MM月dd日 HH:mm:ss");
        return sdf.format(date);
    }

    public static String getHHmmTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }

    public static Long getHHmmSSTime(long time) {
        try {
            String hhmm = getHHmmssTime(time);
            String str1 = hhmm.substring(0, 2);
            String str2 = hhmm.substring(3, 5);
            String str3 = hhmm.substring(6, 8);
            hhmm = str1 + str2 + str3;
            Long sortTime = Long.parseLong(hhmm);
            return sortTime;
        } catch (Exception e) {
            QAILog.e("DateUtils", e);
            return  0L;
        }
    }

    public static Long getSortTime(long time) {
        try {
            String hhmm = getHHmmTime(time);
            String str1 = hhmm.substring(0, 2);
            String str2 = hhmm.substring(3, 5);
            hhmm = str1 + str2;
            Long sortTime = Long.parseLong(hhmm);
            return sortTime;
        } catch (Exception e) {
            QAILog.e("DateUtils", e);
            return  0L;
        }
    }

    public static String getHHmmssTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(date);
    }
    public static Long getToadySortTime(Long time) {
        String yMD = getYearMonthDay();
        String hourTime = getHHmmssTime(time);
        String str = yMD + " " + hourTime;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date != null)
            return date.getTime();
        else
            return 0L;
    }

    public static Long getToadyBeginTime() {
        String yMD = getYearMonthDay();
        String hourTime = " 00:00:00";
        String str = yMD + hourTime;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date != null)
            return date.getTime();
        else
            return 0L;
    }

    public static Long getTodayEndTime() {
        String yMD = getYearMonthDay();
        String hourTime = " 23:59:59";
        String str = yMD + hourTime;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date != null)
            return date.getTime();
        else
            return 0L;
    }

    public static String getRemindTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }

    public static String getTime(String time) {
        return getTime(time, "HH:mm:ss");
    }

    public static String getTime(String time, String format) {
        Date date = new Date(Long.parseLong(time));
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String getDate(String time) {
        Date date = new Date(Long.parseLong(time));
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
        return sdf.format(date);
    }

    public static String getDate(long time) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
        return sdf.format(date);
    }

    public static String getYearMonthDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new java.util.Date());
        return date;
    }

    public static String getCurDumpTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        return dateFormat.format(date);
    }

    public static Long strToDateTime(String str) {
        long dateTime = 0L;
        try {
            String yMD = getYearMonthDay();
            int idx = str.indexOf(" ");
            String hMinu = str.substring(idx + 1);
            int size = hMinu.length();
            if(!hMinu.substring(size-2, size -1).equals("00")) {
                hMinu = hMinu.substring(0, size - 2);
                hMinu += "00";
            }
            String full = yMD + " " + hMinu;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = format.parse(full);
            dateTime = date.getTime();
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();
            if (currentTime > dateTime) {
                calendar.setTimeInMillis(dateTime);
                calendar.add(Calendar.DATE, 1);
                dateTime = calendar.getTimeInMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static Long strToDateTime(String addTime, String str) {
        long dateTime = 0L;
        try {
            int idx1 = addTime.indexOf(" ");
            String yMD = addTime.substring(0, idx1);
            int idx = str.indexOf(" ");
            String moth_day = str.substring(idx + 1);
            int size = moth_day.length();
            if(!moth_day.substring(size-2, size -1).equals("00")) {
                moth_day = moth_day.substring(0, size - 2);
                moth_day += "00";
            }
            String full = yMD + " " + moth_day;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = format.parse(full);
            dateTime = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static Long strToHabitStartDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
            long dateTime = date.getTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateTime);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static Long strToHabitStartDate(String str, String when) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            int idx1 = str.indexOf(" ");
            String yMD = str.substring(0, idx1);
            int idx = when.indexOf(" ");
            String hMs = when.substring(idx + 1);
            int size = hMs.length();
            if(!hMs.substring(size-2, size -1).equals("00")) {
                hMs = hMs.substring(0, size - 2);
                hMs += "00";
            }
            String full = yMD + " " + hMs;
            date = format.parse(full);
            long dateTime = date.getTime();
            Calendar calendar = Calendar.getInstance();
            long current = calendar.getTimeInMillis();
            calendar.setTimeInMillis(dateTime);
            if(dateTime < current) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static Long strToEndDate(String str) {
        int idx = str.indexOf(" ");
        String moth_day = str.substring(0, idx);
        String full = moth_day + " 23:59:59";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(full);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static long strToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        long dateTime;
        try {
            date = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dateTime = date.getTime();
        return dateTime;
    }

    public static long strToRemindDate(String str) {
        if(TextUtils.isEmpty(str))
            return 0L;
        int size = str.indexOf(" ");
        str = str.substring(0, size + 7);
        str = str + "00";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        long dateTime = 0L;
        try {
            date = format.parse(str);
            dateTime = date.getTime();
            if (currentTime > dateTime) {
                calendar.setTimeInMillis(dateTime);
                calendar.add(Calendar.DATE, 1);
                dateTime = calendar.getTimeInMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static int getWeekDay() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        int w = 0;
        if (date != null) {
            calendar.setTime(date);
            w = calendar.get(Calendar.DAY_OF_WEEK);
            w = w - 1;
        }
        return ((w > 0) ? w : 7);
    }

    public static Long strDateToLong(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date != null)
            return date.getTime();
        else
            return 0L;
    }

    public static Long strMonthDateToLong(String str) {
        int idx = str.indexOf(" ");
        String moth_day = str.substring(idx + 1);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(moth_day);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date != null)
            return date.getTime();
        else
            return 0L;
    }

    /**
     * 返回上午下午
     *
     * @param context
     * @param time
     * @return
     */
    public static String getTimeApm(Context context, long time) {
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);

        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
//        if (hour >= 0 && hour < 8) {
//            return context.getResources().getString(R.string.morning_before);
//        } else if (hour >= 8 && hour < 12) {
//            return context.getResources().getString(R.string.morning);
//        } else if (hour >= 12 && hour < 18) {
//            return context.getResources().getString(R.string.afternoon);
//        } else if (hour >= 18 && hour < 24) {
//            return context.getResources().getString(R.string.afternoon_after);
//        }
        if (hour >= 0 && hour < 12) {
            return "上午";
        } else if (hour >= 12 && hour < 24) {
            return "下午";
        }
        return "";
    }

    /**
     * 格式化提醒时间
     *
     * @param time
     * @return
     */
    public static String getFormatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        String dateStr = sdf.format(time);
        return dateStr;
    }

    public static int getWeekOfDate(long dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w <= 0)
            w = 7;
        return w;
    }

    private static int getDiff(int diff, int index, int day) {
        int temp = day - index;
        temp = temp >= 0 ? temp : 7 + temp;
        if (diff > temp) {
            return temp;
        }
        return diff;
    }

    public static Long getReminderTime(Long mTime, String recurr) {
        long time = mTime;
        int index = getWeekOfDate(time);
        int diff = 7;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        StringBuilder builder = new StringBuilder();
        if (recurr.contains("1")) {
            builder.append("1");
            builder.append(",");
            diff = getDiff(diff, index, 1);
        }
        if (recurr.contains("2")) {
            builder.append("2");
            builder.append(",");
            diff = getDiff(diff, index, 2);
        }
        if (recurr.contains("3")) {
            builder.append("3");
            builder.append(",");
            diff = getDiff(diff, index, 3);
        }
        if (recurr.contains("4")) {
            builder.append("4");
            builder.append(",");
            diff = getDiff(diff, index, 4);
        }
        if (recurr.contains("5")) {
            builder.append("5");
            builder.append(",");
            diff = getDiff(diff, index, 5);
        }
        if (recurr.contains("6")) {
            builder.append("6");
            builder.append(",");
            diff = getDiff(diff, index, 6);
        }
        if (recurr.contains("7")) {
            builder.append("7");
            builder.append(",");
            diff = getDiff(diff, index, 7);
        }
        calendar.add(Calendar.DATE, diff);
        long alarmTime = calendar.getTimeInMillis();
        if (alarmTime <= System.currentTimeMillis()) {
            alarmTime += 7 * 24 * 60 * 60 * 1000;
        }
        return alarmTime;
    }
}
