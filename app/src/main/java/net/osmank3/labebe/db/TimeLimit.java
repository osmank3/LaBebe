/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.db;


import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;

public class TimeLimit {
    private Days days;
    private Type type;
    private Date duration;
    private Date start;
    private Date end;
    private String appName;
    private boolean status = true;

    public TimeLimit() {

    }

    public TimeLimit(Days days, Type type, Date duration, Date start, Date end, String appName, boolean status) {
        this.days = days;
        this.type = type;
        this.duration = duration;
        this.start = start;
        this.end = end;
        this.appName = appName;
        this.status = status;
    }

    public static TimeLimit timeLimitFromMap(HashMap hashMap) {
        TimeLimit timeLimit = new TimeLimit();
        if (hashMap.containsKey("days") && hashMap.get("days") != null)
            timeLimit.setDays(TimeLimit.Days.daysFromMap((HashMap)hashMap.get("days")));
        if (hashMap.containsKey("type") && hashMap.get("type") != null) {
            if (hashMap.get("type").equals("LimitedHours"))
                timeLimit.setType(TimeLimit.Type.LimitedHours);
            else if (hashMap.get("type").equals("DailyHours"))
                timeLimit.setType(TimeLimit.Type.DailyHours);
            else if (hashMap.get("type").equals("AppDailyHours"))
                timeLimit.setType(TimeLimit.Type.AppDailyHours);
        }
        if (hashMap.containsKey("duration") && hashMap.get("duration") != null)
            timeLimit.setDuration(((Timestamp)hashMap.get("duration")).toDate());
        if (hashMap.containsKey("start") && hashMap.get("start") != null)
            timeLimit.setStart(((Timestamp)hashMap.get("start")).toDate());
        if (hashMap.containsKey("end") && hashMap.get("end") != null)
            timeLimit.setEnd(((Timestamp)hashMap.get("end")).toDate());
        if (hashMap.containsKey("appName") && hashMap.get("appName") != null)
            timeLimit.setAppName((String) hashMap.get("appName"));
        if (hashMap.containsKey("status") && hashMap.get("status") != null)
            timeLimit.setStatus((Boolean)hashMap.get("status"));

        return timeLimit;
    }

    public static class Days {
        public boolean Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday;
        private int code;

        public Days() {
            Monday = false;
            Tuesday = false;
            Wednesday = false;
            Thursday = false;
            Friday = false;
            Saturday = false;
            Sunday = false;
        }

        public Days(int code) {
            this.code = code;
            Monday = (code & 1) == 1;
            Tuesday = (code & 2) == 2;
            Wednesday = (code & 4) == 4;
            Thursday = (code & 8) == 8;
            Friday = (code & 16) == 16;
            Saturday = (code & 32) == 32;
            Sunday = (code & 64) == 64;
        }

        static public Days daysFromMap(HashMap<String, Boolean> hashMap) {
            Days days = new Days();
            days.Monday = hashMap.get("Monday");
            days.Tuesday = hashMap.get("Tuesday");;
            days.Wednesday = hashMap.get("Wednesday");
            days.Thursday = hashMap.get("Thursday");
            days.Friday = hashMap.get("Friday");
            days.Saturday = hashMap.get("Saturday");
            days.Sunday = hashMap.get("Sunday");
            days.calculate();
            return days;
        }

        private int calculate() {
            code = 0;
            code += Monday ? 1 : 0;
            code += Tuesday ? 2 : 0;
            code += Wednesday ? 4 : 0;
            code += Thursday ? 8 : 0;
            code += Friday ? 16 : 0;
            code += Saturday ? 32 : 0;
            code += Sunday ? 64 : 0;
            return code;
        }

        public static Days getDays(int numeral){
            return new Days(numeral);
        }

        public static int getDaysInt(Days days) {
            return days.calculate();
        }

        public int getCode() {
            calculate();
            return code;
        }
    }

    public enum Type {
        LimitedHours(0),
        DailyHours(1),
        AppDailyHours(2);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static int getTypeInt(Type type) {
            return type.code;
        }

        public static Type getType(int numeral) {
            for (Type type: values()) {
                if (type.code == numeral) {
                    return type;
                }
            }
            return null;
        }
    }

    public Days getDays() {
        return days;
    }

    public void setDays(Days days) {
        this.days = days;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getDuration() {
        return duration;
    }

    public void setDuration(Date duration) {
        this.duration = duration;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
