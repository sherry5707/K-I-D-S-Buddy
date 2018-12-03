package com.kinstalk.her.qchatmodel.recurrence;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.TimeFormatException;

import com.kinstalk.her.qchatcomm.utils.QAILog;

import java.util.Arrays;
import java.util.Calendar;

import static java.lang.Integer.parseInt;

public class RecurrenceHelper {
    private final int[] TIME_DAY_TO_CALENDAR_DAY = new int[]{
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
    };
    private static final int[] mFreqModelToEventRecurrence = {
            EventRecurrence.DAILY,
            EventRecurrence.WEEKLY,
            EventRecurrence.MONTHLY,
            EventRecurrence.YEARLY
    };
    // Special cases in monthlyByNthDayOfWeek
    private static final int FIFTH_WEEK_IN_A_MONTH = 5;
    private static final int LAST_NTH_DAY_OF_WEEK = -1;
    // Update android:maxLength in EditText as needed
    private static final int INTERVAL_MAX = 99;
    private static final int INTERVAL_DEFAULT = 1;
    // Update android:maxLength in EditText as needed
    private static final int COUNT_MAX = 730;
    private static final int COUNT_DEFAULT = 5;
    public static final int MONTHLY_BY_DATE = 0;

    private EventRecurrence mRecurrence = new EventRecurrence();
    private Time mTime = new Time(); // TODO timezone?
    private RecurrenceModel mModel = new RecurrenceModel();

    public RecurrenceHelper() {

    }

    private class RecurrenceModel implements Parcelable {

        // Should match EventRecurrence.DAILY, etc
        static final int FREQ_DAILY = 0;
        static final int FREQ_WEEKLY = 1;
        static final int FREQ_MONTHLY = 2;
        static final int FREQ_YEARLY = 3;

        static final int END_NEVER = 0;
        static final int END_BY_DATE = 1;
        static final int END_BY_COUNT = 2;

        static final int MONTHLY_BY_NTH_DAY_OF_WEEK = 1;

        static final int STATE_NO_RECURRENCE = 0;
        static final int STATE_RECURRENCE = 1;

        int recurrenceState;

        /**
         * FREQ: Repeat pattern
         *
         * @see FREQ_DAILY
         * @see FREQ_WEEKLY
         * @see FREQ_MONTHLY
         * @see FREQ_YEARLY
         */
        int freq = FREQ_WEEKLY;

        /**
         * INTERVAL: Every n days/weeks/months/years. n >= 1
         */
        int interval = INTERVAL_DEFAULT;

        /**
         * UNTIL and COUNT: How does the the event end?
         *
         * @see END_NEVER
         * @see END_BY_DATE
         * @see END_BY_COUNT
         * @see untilDate
         * @see untilCount
         */
        int end;

        /**
         * UNTIL: Date of the last recurrence. Used when until == END_BY_DATE
         */
        Time endDate;

        /**
         * COUNT: Times to repeat. Use when until == END_BY_COUNT
         */
        int endCount = COUNT_DEFAULT;

        /**
         * BYDAY: Days of the week to be repeated. Sun = 0, Mon = 1, etc
         */
        boolean[] weeklyByDayOfWeek = new boolean[7];

        /**
         * BYDAY AND BYMONTHDAY: How to repeat monthly events? Same date of the
         * month or Same nth day of week.
         *
         * @see MONTHLY_BY_DATE
         * @see MONTHLY_BY_NTH_DAY_OF_WEEK
         */
        int monthlyRepeat;

        /**
         * Day of the month to repeat. Used when monthlyRepeat ==
         * MONTHLY_BY_DATE
         */
        int monthlyByMonthDay;

        /**
         * Day of the week to repeat. Used when monthlyRepeat ==
         * MONTHLY_BY_NTH_DAY_OF_WEEK
         */
        int monthlyByDayOfWeek;

        /**
         * Nth day of the week to repeat. Used when monthlyRepeat ==
         * MONTHLY_BY_NTH_DAY_OF_WEEK 0=undefined, -1=Last, 1=1st, 2=2nd, ..., 5=5th
         * <p>
         * We support 5th, just to handle backwards capabilities with old bug, but it
         * gets converted to -1 once edited.
         */
        int monthlyByNthDayOfWeek;

        protected RecurrenceModel(Parcel in) {
            recurrenceState = in.readInt();
            freq = in.readInt();
            interval = in.readInt();
            end = in.readInt();
            endCount = in.readInt();
            weeklyByDayOfWeek = in.createBooleanArray();
            monthlyRepeat = in.readInt();
            monthlyByMonthDay = in.readInt();
            monthlyByDayOfWeek = in.readInt();
            monthlyByNthDayOfWeek = in.readInt();
        }

        public final Creator<RecurrenceModel> CREATOR = new Creator<RecurrenceModel>() {
            @Override
            public RecurrenceModel createFromParcel(Parcel in) {
                return new RecurrenceModel(in);
            }

            @Override
            public RecurrenceModel[] newArray(int size) {
                return new RecurrenceModel[size];
            }
        };

        /*
                 * (generated method)
                 */
        @Override
        public String toString() {
            return "Model [freq=" + freq + ", interval=" + interval + ", end=" + end + ", endDate="
                    + endDate + ", endCount=" + endCount + ", weeklyByDayOfWeek="
                    + Arrays.toString(weeklyByDayOfWeek) + ", monthlyRepeat=" + monthlyRepeat
                    + ", monthlyByMonthDay=" + monthlyByMonthDay + ", monthlyByDayOfWeek="
                    + monthlyByDayOfWeek + ", monthlyByNthDayOfWeek=" + monthlyByNthDayOfWeek + "]";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public RecurrenceModel() {
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(freq);
            dest.writeInt(interval);
            dest.writeInt(end);
            dest.writeInt(endDate.year);
            dest.writeInt(endDate.month);
            dest.writeInt(endDate.monthDay);
            dest.writeInt(endCount);
            dest.writeBooleanArray(weeklyByDayOfWeek);
            dest.writeInt(monthlyRepeat);
            dest.writeInt(monthlyByMonthDay);
            dest.writeInt(monthlyByDayOfWeek);
            dest.writeInt(monthlyByNthDayOfWeek);
            dest.writeInt(recurrenceState);
        }
    }

    // TODO don't lose data when getting data that our UI can't handle
    static private void copyEventRecurrenceToModel(final EventRecurrence er,
                                                   RecurrenceModel model) {
        // Freq:
        switch (er.freq) {
            case EventRecurrence.DAILY:
                model.freq = RecurrenceModel.FREQ_DAILY;
                break;
            case EventRecurrence.MONTHLY:
                model.freq = RecurrenceModel.FREQ_MONTHLY;
                break;
            case EventRecurrence.YEARLY:
                model.freq = RecurrenceModel.FREQ_YEARLY;
                break;
            case EventRecurrence.WEEKLY:
                model.freq = RecurrenceModel.FREQ_WEEKLY;
                break;
            default:
                throw new IllegalStateException("freq=" + er.freq);
        }

        // Interval:
        if (er.interval > 0) {
            model.interval = er.interval;
        }

        // End:
        // End by count:
        model.endCount = er.count;
        if (model.endCount > 0) {
            model.end = RecurrenceModel.END_BY_COUNT;
        }

        // End by date:
        if (!TextUtils.isEmpty(er.until)) {
            if (model.endDate == null) {
                model.endDate = new Time();
            }

            try {
                model.endDate.parse(er.until);
            } catch (TimeFormatException e) {
                model.endDate = null;
            }

            // LIMITATION: The UI can only handle END_BY_DATE or END_BY_COUNT
            if (model.end == RecurrenceModel.END_BY_COUNT && model.endDate != null) {
                throw new IllegalStateException("freq=" + er.freq);
            }

            model.end = RecurrenceModel.END_BY_DATE;
        }

        // Weekly: repeat by day of week or Monthly: repeat by nth day of week
        // in the month
        Arrays.fill(model.weeklyByDayOfWeek, false);
        if (er.bydayCount > 0) {
            int count = 0;
            for (int i = 0; i < er.bydayCount; i++) {
                int dayOfWeek = EventRecurrence.day2TimeDay(er.byday[i]);
                model.weeklyByDayOfWeek[dayOfWeek] = true;

                if (model.freq == RecurrenceModel.FREQ_MONTHLY &&
                        isSupportedMonthlyByNthDayOfWeek(er.bydayNum[i])) {
                    // LIMITATION: Can handle only (one) weekDayNum in nth or last and only
                    // when
                    // monthly
                    model.monthlyByDayOfWeek = dayOfWeek;
                    model.monthlyByNthDayOfWeek = er.bydayNum[i];
                    model.monthlyRepeat = RecurrenceModel.MONTHLY_BY_NTH_DAY_OF_WEEK;
                    count++;
                }
            }

            if (model.freq == RecurrenceModel.FREQ_MONTHLY) {
                if (er.bydayCount != 1) {
                    // Can't handle 1st Monday and 2nd Wed
                    throw new IllegalStateException("Can handle only 1 byDayOfWeek in monthly");
                }
                if (count != 1) {
                    throw new IllegalStateException(
                            "Didn't specify which nth day of week to repeat for a monthly");
                }
            }
        }

        // Monthly by day of month
        if (model.freq == RecurrenceModel.FREQ_MONTHLY) {
            if (er.bymonthdayCount == 1) {
                if (model.monthlyRepeat == RecurrenceModel.MONTHLY_BY_NTH_DAY_OF_WEEK) {
                    throw new IllegalStateException(
                            "Can handle only by monthday or by nth day of week, not both");
                }
                model.monthlyByMonthDay = er.bymonthday[0];
                model.monthlyRepeat = MONTHLY_BY_DATE;
            } else if (er.bymonthCount > 1) {
                // LIMITATION: Can handle only one month day
                throw new IllegalStateException("Can handle only one bymonthday");
            }
        }
    }

    static private void copyModelToEventRecurrence(final RecurrenceModel model,
                                                   EventRecurrence er) {
        if (model.recurrenceState == RecurrenceModel.STATE_NO_RECURRENCE) {
            throw new IllegalStateException("There's no recurrence");
        }

        // Freq
        er.freq = mFreqModelToEventRecurrence[model.freq];

        // Interval
        if (model.interval <= 1) {
            er.interval = 0;
        } else {
            er.interval = model.interval;
        }

        // End
        switch (model.end) {
            case RecurrenceModel.END_BY_DATE:
                if (model.endDate != null) {
                    model.endDate.switchTimezone(Time.TIMEZONE_UTC);
                    model.endDate.normalize(false);
                    er.until = model.endDate.format2445();
                    er.count = 0;
                } else {
                    throw new IllegalStateException("end = END_BY_DATE but endDate is null");
                }
                break;
            case RecurrenceModel.END_BY_COUNT:
                er.count = model.endCount;
                er.until = null;
                if (er.count <= 0) {
                    throw new IllegalStateException("count is " + er.count);
                }
                break;
            default:
                er.count = 0;
                er.until = null;
                break;
        }

        // Weekly && monthly repeat patterns
        er.bydayCount = 0;
        er.bymonthdayCount = 0;

        switch (model.freq) {
            case RecurrenceModel.FREQ_MONTHLY:
                if (model.monthlyRepeat == MONTHLY_BY_DATE) {
                    if (model.monthlyByMonthDay > 0) {
                        if (er.bymonthday == null || er.bymonthdayCount < 1) {
                            er.bymonthday = new int[1];
                        }
                        er.bymonthday[0] = model.monthlyByMonthDay;
                        er.bymonthdayCount = 1;
                    }
                } else if (model.monthlyRepeat == RecurrenceModel.MONTHLY_BY_NTH_DAY_OF_WEEK) {
                    if (!isSupportedMonthlyByNthDayOfWeek(model.monthlyByNthDayOfWeek)) {
                        throw new IllegalStateException("month repeat by nth week but n is "
                                + model.monthlyByNthDayOfWeek);
                    }
                    int count = 1;
                    if (er.bydayCount < count || er.byday == null || er.bydayNum == null) {
                        er.byday = new int[count];
                        er.bydayNum = new int[count];
                    }
                    er.bydayCount = count;
                    er.byday[0] = EventRecurrence.timeDay2Day(model.monthlyByDayOfWeek);
                    er.bydayNum[0] = model.monthlyByNthDayOfWeek;
                }
                break;
            case RecurrenceModel.FREQ_WEEKLY:
                int count = 0;
                for (int i = 0; i < 7; i++) {
                    if (model.weeklyByDayOfWeek[i]) {
                        count++;
                    }
                }

                if (er.bydayCount < count || er.byday == null || er.bydayNum == null) {
                    er.byday = new int[count];
                    er.bydayNum = new int[count];
                }
                er.bydayCount = count;

                for (int i = 6; i >= 0; i--) {
                    if (model.weeklyByDayOfWeek[i]) {
                        er.bydayNum[--count] = 0;
                        er.byday[count] = EventRecurrence.timeDay2Day(i);
                    }
                }
                break;
        }

        if (!canHandleRecurrenceRule(er)) {
            throw new IllegalStateException("UI generated recurrence that it can't handle. ER:"
                    + er.toString() + " Model: " + model.toString());
        }
    }

    static public boolean canHandleRecurrenceRule(EventRecurrence er) {
        switch (er.freq) {
            case EventRecurrence.DAILY:
            case EventRecurrence.MONTHLY:
            case EventRecurrence.YEARLY:
            case EventRecurrence.WEEKLY:
                break;
            default:
                return false;
        }

        if (er.count > 0 && !TextUtils.isEmpty(er.until)) {
            return false;
        }

        // Weekly: For "repeat by day of week", the day of week to repeat is in
        // er.byday[]

        /*
         * Monthly: For "repeat by nth day of week" the day of week to repeat is
         * in er.byday[] and the "nth" is stored in er.bydayNum[]. Currently we
         * can handle only one and only in monthly
         */
        int numOfByDayNum = 0;
        for (int i = 0; i < er.bydayCount; i++) {
            if (isSupportedMonthlyByNthDayOfWeek(er.bydayNum[i])) {
                ++numOfByDayNum;
            }
        }

        if (numOfByDayNum > 1) {
            return false;
        }

        if (numOfByDayNum > 0 && er.freq != EventRecurrence.MONTHLY) {
            return false;
        }

        // The UI only handle repeat by one day of month i.e. not 9th and 10th
        // of every month
        if (er.bymonthdayCount > 1) {
            return false;
        }

        if (er.freq == EventRecurrence.MONTHLY) {
            if (er.bydayCount > 1) {
                return false;
            }
            if (er.bydayCount > 0 && er.bymonthdayCount > 0) {
                return false;
            }
        }

        return true;
    }

    static public boolean isSupportedMonthlyByNthDayOfWeek(int num) {
        // We only support monthlyByNthDayOfWeek when it is greater then 0 but less then 5.
        // Or if -1 when it is the last monthly day of the week.
        return (num > 0 && num <= FIFTH_WEEK_IN_A_MONTH) || num == LAST_NTH_DAY_OF_WEEK;
    }

    /**
     * 获取重复规则
     *
     * @param context
     * @return
     */
    public static String getRruleValue(Context context, String rrule) {
        EventRecurrence recurrence = new EventRecurrence();
        recurrence.parse(rrule);
        return EventRecurrenceFormatter.getRepeatString(context, context.getResources(), recurrence, false);
    }

    /** 获取数字规则
     *
     * @param repeat
     * @return
     */
    public static String getNumericRruleValue(Context context, String rruel) {
        EventRecurrence recurrence = new EventRecurrence();
        recurrence.parse(rruel);
        return EventRecurrenceFormatter.getRepeatNumericString(context,context.getResources(),recurrence, false);
    }

    public String getRrule(String repeat) {
        return getRrule(repeat, 0, 0L);
    }

    public String getRrule(String repeat, int duration) {
        return getRrule(repeat, duration, 0L);
    }

    public String getRrule(String repeat, Long millis) {
        return getRrule(repeat, 0, millis);
    }

    public String getRrule(String repeat, int duration, Long millis) {
        if (TextUtils.isEmpty(repeat)) {
            return null;
        }
        if (TextUtils.equals("EVERYDAY", repeat)) {
            mModel.freq = RecurrenceModel.FREQ_DAILY;
        } else if (repeat.startsWith("W")) {
            mModel.freq = RecurrenceModel.FREQ_WEEKLY;
            String[] dayArr = repeat.substring(1, repeat.length()).split(",");
            for (int i = 0; i < dayArr.length; i++) {
                int index = Integer.parseInt(dayArr[i]);
                if (index == 7) {
                    mModel.weeklyByDayOfWeek[0] = true;
                } else {
                    mModel.weeklyByDayOfWeek[index] = true;
                }
            }
        } else if (repeat.startsWith("M")) {
            mModel.freq = RecurrenceModel.FREQ_MONTHLY;
            mModel.monthlyRepeat = MONTHLY_BY_DATE;
            mModel.monthlyByMonthDay = parseInt(repeat.substring(1, repeat.length()));
        }

        mModel.recurrenceState = RecurrenceModel.STATE_RECURRENCE;
        if(duration != 0) {
            mModel.end = RecurrenceModel.END_BY_COUNT;
            mModel.endCount = duration;
        } else if(millis != 0) {
            mModel.end = RecurrenceModel.END_BY_DATE;
            Time time = new Time();
            time.set(millis);
            mModel.endDate = time;
        }else{
            mModel.end = RecurrenceModel.END_NEVER;
        }
        copyModelToEventRecurrence(mModel, mRecurrence);
        String rrule = mRecurrence.toString();
        return rrule;
    }

    public String getRrule(int freq) {
        mModel.recurrenceState = RecurrenceModel.STATE_RECURRENCE;
        mModel.end = RecurrenceModel.END_NEVER;
        mModel.freq = freq;
        switch (freq) {
            case EventRecurrence.DAILY:

                break;
            case EventRecurrence.WEEKLY:
                mModel.weeklyByDayOfWeek = new boolean[]{false, false, false, true, false, false, false};
                break;
            case EventRecurrence.MONTHLY:
                mModel.monthlyRepeat = MONTHLY_BY_DATE;
                mModel.monthlyByMonthDay = 1;
                break;
            case EventRecurrence.YEARLY:
                break;
        }
        copyModelToEventRecurrence(mModel, mRecurrence);
        String rrule = mRecurrence.toString();
        return rrule;
    }

    public String getRruleDaily() {
        mModel.freq = RecurrenceModel.FREQ_DAILY;
        mModel.recurrenceState = RecurrenceModel.STATE_RECURRENCE;
        mModel.end = RecurrenceModel.END_NEVER;
        copyModelToEventRecurrence(mModel, mRecurrence);
        String rrule = mRecurrence.toString();
        return rrule;
    }

    public String getRruleWeekly(boolean[] weekly) {
        mModel.freq = RecurrenceModel.FREQ_WEEKLY;
        mModel.weeklyByDayOfWeek = weekly;
        mModel.recurrenceState = RecurrenceModel.STATE_RECURRENCE;
        mModel.end = RecurrenceModel.END_NEVER;
        copyModelToEventRecurrence(mModel, mRecurrence);
        String rrule = mRecurrence.toString();
        return rrule;
    }

}
