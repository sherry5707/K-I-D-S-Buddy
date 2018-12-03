package com.kinstalk.her.qchatmodel.recurrence;
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.TimeFormatException;
import java.util.Calendar;

import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.R;

public class EventRecurrenceFormatter {

    private static int[] mMonthRepeatByDayOfWeekIds;
    private static String[][] mMonthRepeatByDayOfWeekStrs;

    public static String getRepeatString(Context context, Resources r, EventRecurrence recurrence,
                                         boolean includeEndString) {
        String endString = "";
        if (includeEndString) {
            StringBuilder sb = new StringBuilder();
            if (recurrence.until != null) {
                try {
                    Time t = new Time();
                    t.parse(recurrence.until);
                    final String dateStr = DateUtils.formatDateTime(context,
                            t.toMillis(false), DateUtils.FORMAT_NUMERIC_DATE);
                    sb.append(r.getString(R.string.endByDate, dateStr));
                } catch (TimeFormatException e) {
                }
            }

            if (recurrence.count > 0) {
                sb.append(r.getQuantityString(R.plurals.endByCount, recurrence.count,
                        recurrence.count));
            }
            endString = sb.toString();
        }
        QAILog.d("recurrence", "enter");
        // TODO Implement "Until" portion of string, as well as custom settings
        int interval = recurrence.interval <= 1 ? 1 : recurrence.interval;
        switch (recurrence.freq) {
            case EventRecurrence.DAILY:
//                return r.getQuantityString(R.plurals.daily, interval, interval) + endString;
                return r.getString(R.string.everyday);
            case EventRecurrence.WEEKLY: {
//                int day = EventRecurrence.timeDay2Day(recurrence.startDate.weekDay);
//                String value = dayToString(day, DateUtils.LENGTH_LONG);
//                return r.getString(R.string.everyweek, value);
//                if (recurrence.repeatsOnEveryWeekDay()) {
//                    return r.getString(R.string.every_weekday) + endString;
//                } else {
                QAILog.d("recurrence", "weekly");
                if (recurrence.repeatsOnEveryWeekDay()) {
                    return "工作日";
                } else if (recurrence.repeatsOnEveryWeekend()) {
                    return "周末";
                } else {
                    String string;

                    int dayOfWeekLength = DateUtils.LENGTH_MEDIUM;
                    if (recurrence.bydayCount == 1) {
                        dayOfWeekLength = DateUtils.LENGTH_LONG;
                    }

                    StringBuilder days = new StringBuilder();

                    // Do one less iteration in the loop so the last element is added out of the
                    // loop. This is done so the comma is not placed after the last item.

                    if (recurrence.bydayCount > 0) {
                        int count = recurrence.bydayCount - 1;
                        for (int i = 0; i < count; i++) {
                            days.append(dayToString(recurrence.byday[i]));
                            days.append("、");
                        }
                        days.append(dayToString(recurrence.byday[count]));

                        string = days.toString();
                        QAILog.d("recurrence", string + "days");
                    } else {
                        // There is no "BYDAY" specifier, so use the day of the
                        // first event.  For this to work, the setStartDate()
                        // method must have been used by the caller to set the
                        // date of the first event in the recurrence.
                        if (recurrence.startDate == null) {
                            return null;
                        }

                        int day = EventRecurrence.timeDay2Day(recurrence.startDate.weekDay);
                        QAILog.d("recurrence", +day + "int");
                        string = dayToString(day, DateUtils.LENGTH_LONG);
                        QAILog.d("recurrence", string + "daytostring");
                    }
                    return r.getString(R.string.everyweek, string);
//                    return r.getQuantityString(R.plurals.weekly, interval, interval, string)
//                            + endString;
                }
            }
            case EventRecurrence.MONTHLY: {
//                return r.getString(R.string.everymonth);
                if (recurrence.bydayCount == 1) {
                    int weekday = recurrence.startDate.weekDay;
//                    // Cache this stuff so we won't have to redo work again later.
                    cacheMonthRepeatStrings(r, weekday);
                    int dayNumber = (recurrence.startDate.monthDay - 1) / 7;
                    StringBuilder sb = new StringBuilder();
                    sb.append(r.getString(R.string.everymonth));
                    sb.append(" (");
                    sb.append(mMonthRepeatByDayOfWeekStrs[weekday][dayNumber]);
                    sb.append(")");
                    sb.append(endString);
                    return sb.toString();
                }
                return r.getString(R.string.everymonth) + endString;
            }
            case EventRecurrence.YEARLY:
//                return r.getString(R.string.yearly_plain) + endString;
                return r.getString(R.string.everyyear);
        }

        return null;
    }

    public static String getRepeatNumericString(Context context, Resources r, EventRecurrence recurrence,
                                         boolean includeEndString) {
        String endString = "";
        if (includeEndString) {
            StringBuilder sb = new StringBuilder();
            if (recurrence.until != null) {
                try {
                    Time t = new Time();
                    t.parse(recurrence.until);
                    final String dateStr = DateUtils.formatDateTime(context,
                            t.toMillis(false), DateUtils.FORMAT_NUMERIC_DATE);
                    sb.append(r.getString(R.string.endByDate, dateStr));
                } catch (TimeFormatException e) {
                }
            }

            if (recurrence.count > 0) {
                sb.append(r.getQuantityString(R.plurals.endByCount, recurrence.count,
                        recurrence.count));
            }
            endString = sb.toString();
        }

        // TODO Implement "Until" portion of string, as well as custom settings
        int interval = recurrence.interval <= 1 ? 1 : recurrence.interval;
        switch (recurrence.freq) {
            case EventRecurrence.DAILY:
                return "1,2,3,4,5,6,7";
            case EventRecurrence.WEEKLY: {
                if (recurrence.repeatsOnEveryWeekDay()) {
                    return "1,2,3,4,5";
                } else if (recurrence.repeatsOnEveryWeekend()) {
                    return "6,7";
                } else {
                    String string;

                    int dayOfWeekLength = DateUtils.LENGTH_MEDIUM;
                    if (recurrence.bydayCount == 1) {
                        dayOfWeekLength = DateUtils.LENGTH_LONG;
                    }

                    StringBuilder days = new StringBuilder();

                    // Do one less iteration in the loop so the last element is added out of the
                    // loop. This is done so the comma is not placed after the last item.

                    if (recurrence.bydayCount > 0) {
                        int count = recurrence.bydayCount - 1;
                        for (int i = 0; i < count; i++) {
                            days.append(dayToUtilString(recurrence.byday[i]));
                            days.append(",");
                        }
                        days.append(dayToUtilString(recurrence.byday[count]));

                        string = days.toString();
                    } else {
                        // There is no "BYDAY" specifier, so use the day of the
                        // first event.  For this to work, the setStartDate()
                        // method must have been used by the caller to set the
                        // date of the first event in the recurrence.
                        if (recurrence.startDate == null) {
                            return null;
                        }

                        int day = EventRecurrence.timeDay2Day(recurrence.startDate.weekDay);
                        string = dayToString(day, DateUtils.LENGTH_LONG);
                    }
                    return string;
//                    return r.getQuantityString(R.plurals.weekly, interval, interval, string)
//                            + endString;
                }
            }
            case EventRecurrence.MONTHLY: {
//                return r.getString(R.string.everymonth);
                if (recurrence.bydayCount == 1) {
                    int weekday = recurrence.startDate.weekDay;
//                    // Cache this stuff so we won't have to redo work again later.
                    cacheMonthRepeatStrings(r, weekday);
                    int dayNumber = (recurrence.startDate.monthDay - 1) / 7;
                    StringBuilder sb = new StringBuilder();
                    sb.append(r.getString(R.string.everymonth));
                    sb.append(" (");
                    sb.append(mMonthRepeatByDayOfWeekStrs[weekday][dayNumber]);
                    sb.append(")");
                    sb.append(endString);
                    return sb.toString();
                }
                return r.getString(R.string.everymonth) + endString;
            }
            case EventRecurrence.YEARLY:
//                return r.getString(R.string.yearly_plain) + endString;
                return r.getString(R.string.everyyear);
        }

        return null;
    }

    private static void cacheMonthRepeatStrings(Resources r, int weekday) {
        if (mMonthRepeatByDayOfWeekIds == null) {
            mMonthRepeatByDayOfWeekIds = new int[7];
            mMonthRepeatByDayOfWeekIds[0] = R.array.repeat_by_nth_sun;
            mMonthRepeatByDayOfWeekIds[1] = R.array.repeat_by_nth_mon;
            mMonthRepeatByDayOfWeekIds[2] = R.array.repeat_by_nth_tues;
            mMonthRepeatByDayOfWeekIds[3] = R.array.repeat_by_nth_wed;
            mMonthRepeatByDayOfWeekIds[4] = R.array.repeat_by_nth_thurs;
            mMonthRepeatByDayOfWeekIds[5] = R.array.repeat_by_nth_fri;
            mMonthRepeatByDayOfWeekIds[6] = R.array.repeat_by_nth_sat;
        }
        if (mMonthRepeatByDayOfWeekStrs == null) {
            mMonthRepeatByDayOfWeekStrs = new String[7][];
        }
        if (mMonthRepeatByDayOfWeekStrs[weekday] == null) {
            mMonthRepeatByDayOfWeekStrs[weekday] =
                    r.getStringArray(mMonthRepeatByDayOfWeekIds[weekday]);
        }
    }

    /**
     * Converts day of week to a String.
     *
     * @param day a EventRecurrence constant
     * @return day of week as a string
     */
    private static String dayToString(int day, int dayOfWeekLength) {
        return DateUtils.getDayOfWeekString(dayToUtilDay(day), dayOfWeekLength);
    }

    private static String dayToString(int day) {
        switch (day) {
            case EventRecurrence.SU:
                return "日";
            case EventRecurrence.MO:
                return "一";
            case EventRecurrence.TU:
                return "二";
            case EventRecurrence.WE:
                return "三";
            case EventRecurrence.TH:
                return "四";
            case EventRecurrence.FR:
                return "五";
            case EventRecurrence.SA:
                return "六";
            default:
                throw new IllegalArgumentException("bad day argument: " + day);
        }
    }

    private static String dayToUtilString(int day) {
        switch (day) {
            case EventRecurrence.SU:
                return "7";
            case EventRecurrence.MO:
                return "1";
            case EventRecurrence.TU:
                return "2";
            case EventRecurrence.WE:
                return "3";
            case EventRecurrence.TH:
                return "4";
            case EventRecurrence.FR:
                return "5";
            case EventRecurrence.SA:
                return "6";
            default:
                throw new IllegalArgumentException("bad day argument: " + day);
        }
    }
    /**
     * Converts EventRecurrence's day of week to DateUtil's day of week.
     *
     * @param day of week as an EventRecurrence value
     * @return day of week as a DateUtil value.
     */
    private static int dayToUtilDay(int day) {
        switch (day) {
            case EventRecurrence.SU:
                return Calendar.SUNDAY;
            case EventRecurrence.MO:
                return Calendar.MONDAY;
            case EventRecurrence.TU:
                return Calendar.TUESDAY;
            case EventRecurrence.WE:
                return Calendar.WEDNESDAY;
            case EventRecurrence.TH:
                return Calendar.THURSDAY;
            case EventRecurrence.FR:
                return Calendar.FRIDAY;
            case EventRecurrence.SA:
                return Calendar.SATURDAY;
            default:
                throw new IllegalArgumentException("bad day argument: " + day);
        }
    }
}
