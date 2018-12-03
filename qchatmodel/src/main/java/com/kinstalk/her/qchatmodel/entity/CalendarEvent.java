package com.kinstalk.her.qchatmodel.entity;

import java.util.Comparator;

/**
 * Created by Tracy on 2018/4/21.
 */

public class CalendarEvent {
    private String calendarId;
    private String eventId;
    private String title;
    private int type;
    private long startTime;
    private String rrule;
    private String rruleFormat;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getRrule() {
        return rrule;
    }

    public void setRrule(String rrule) {
        this.rrule = rrule;
    }

    public String getRruleFormat() {
        return rruleFormat;
    }

    public void setRruleFormat(String rruleFormat) {
        this.rruleFormat = rruleFormat;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public static Comparator<CalendarEvent> TimeAscComparator = new Comparator<CalendarEvent>() {

        @Override
        public int compare(CalendarEvent lhs, CalendarEvent rhs) {
            long result = lhs.getStartTime() - rhs.getStartTime();
            if (result > 0) {
                return 1;
            } else if (result < 0) {
                return -1;
            }

            return 0;
        }
    };
}
