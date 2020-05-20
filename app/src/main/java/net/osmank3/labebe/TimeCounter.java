/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import net.osmank3.labebe.db.AppLog;
import net.osmank3.labebe.db.Child;
import net.osmank3.labebe.db.TimeLimit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

public class TimeCounter {
    private Child child;
    private List<TimeLimit> timeLimits;
    private List<CountDownTimer> timers;
    private String focusedApp;
    private TimeLimit.Type reason;
    private LaBebeAccessibilityService accessibility;
    private SharedPreferences preferences;
    private FirebaseFirestore database;
    private AppLog appLog;

    TimeCounter(Child child, List<TimeLimit> timeLimits) {
        this.child = child;
        this.timeLimits = timeLimits;
        accessibility = LaBebeAccessibilityService.instance;
        preferences = accessibility.getBaseContext().getSharedPreferences(accessibility.getBaseContext().getResources().getString(R.string.app_name), MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();
        timers = new ArrayList<>();
    }

    TimeCounter(Child child, List<TimeLimit> timeLimits, String focusedApp) {
        this(child, timeLimits);
        this.focusedApp = focusedApp;
    }

    public Boolean canAccessNow() {
        stop();
        Boolean status = true;
        Calendar now = Calendar.getInstance();
        int day = setToday(now.get(Calendar.DAY_OF_WEEK));
        now.setTimeZone(TimeZone.getTimeZone("UTC"));
        now.clear(Calendar.YEAR);
        now.clear(Calendar.MONTH);
        now.set(Calendar.DAY_OF_YEAR, 1);
        now.setTimeInMillis(now.getTimeInMillis());
        for (final TimeLimit timeLimit: timeLimits) {
            if (!timeLimit.isStatus())
                continue;
            if (timeLimit.getType().equals(TimeLimit.Type.LimitedHours) && (timeLimit.getDays().getCode() & day) == day) {
                Calendar start = Calendar.getInstance();
                start.setTimeZone(TimeZone.getTimeZone("UTC"));
                start.setTime(timeLimit.getStart());
                Calendar end = Calendar.getInstance();
                end.setTimeZone(TimeZone.getTimeZone("UTC"));
                end.setTime(timeLimit.getEnd());
                if (
                        end.after(start) && (now.after(start) && now.after(end)) ||
                        end.after(start) && (end.after(now) && start.after(now)) ||
                        start.after(end) && (now.after(end) && start.after(now))
                ) {
                    status = false;
                    reason = TimeLimit.Type.LimitedHours;
                    break;
                } else {
                    long millisInFuture = 0;
                    if (start.after(end))
                        millisInFuture = 86400000 + end.getTimeInMillis() - now.getTimeInMillis();
                    else
                        millisInFuture = end.getTimeInMillis() - now.getTimeInMillis();
                    CountDownTimer timer = new CountDownTimer(millisInFuture, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            LaBebeAccessibilityService.instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                            Toast toast = Toast.makeText(accessibility.getBaseContext(), R.string.you_are_in_limited_hours, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            stop();
                        }
                    };
                    timers.add(timer);
                }
            } else if (timeLimit.getType().equals(TimeLimit.Type.DailyHours) && (timeLimit.getDays().getCode() & day) == day) {
                Calendar duration = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                duration.setTime(timeLimit.getDuration());
                if (duration.getTime().getTime() <= 0) {
                    status = false;
                    reason = TimeLimit.Type.DailyHours;
                    break;
                } else {
                    CountDownTimer timer = new CountDownTimer(timeLimit.getDuration().getTime(), 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            LaBebeAccessibilityService.instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                            Toast toast = Toast.makeText(accessibility.getBaseContext(), R.string.you_cannot_use_today, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            stop();
                        }
                    };
                    timers.add(timer);
                }
            } else if (timeLimit.getType().equals(TimeLimit.Type.AppDailyHours) && timeLimit.getAppName().equals(focusedApp)) {
                if (timeLimit.getDuration().getTime() <= 0) {
                    status = false;
                    reason = TimeLimit.Type.AppDailyHours;
                    break;
                } else {
                    CountDownTimer timer = new CountDownTimer(timeLimit.getDuration().getTime(), 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            LaBebeAccessibilityService.instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                            Toast toast = Toast.makeText(accessibility.getBaseContext(), R.string.you_cannot_use_today_app, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            stop();
                        }
                    };
                    timers.add(timer);
                }
            }
        }

        return status;
    }

    private int setToday(int today) {
        int day = 0;
        if (today == Calendar.MONDAY) {
            day = 1;
        } else if (today == Calendar.TUESDAY) {
            day = 2;
        } else if (today == Calendar.WEDNESDAY) {
            day = 4;
        } else if (today == Calendar.THURSDAY) {
            day = 8;
        } else if (today == Calendar.FRIDAY) {
            day = 16;
        } else if (today == Calendar.SATURDAY) {
            day = 32;
        } else if (today == Calendar.SUNDAY) {
            day = 64;
        }
        return day;
    }

    public void setFocusedApp(String focusedApp) {
        this.focusedApp = focusedApp;
    }

    public TimeLimit.Type getReason() {
        return reason;
    }

    public void start() {
        for (CountDownTimer timer: timers) {
            timer.start();
        }
        appLog = new AppLog(focusedApp, Calendar.getInstance().getTime(), null);
    }

    public void stop() {
        for (CountDownTimer timer: timers) {
            timer.cancel();
        }
        timers = new ArrayList<>();
        if (appLog != null) {
            appLog.setEnd(Calendar.getInstance().getTime());
            database.collection("users")
                    .document(preferences.getString("userUid", ""))
                    .collection("children")
                    .document(child.getId())
                    .collection("logs")
                    .add(appLog);
            appLog = null;
        }
    }
}
